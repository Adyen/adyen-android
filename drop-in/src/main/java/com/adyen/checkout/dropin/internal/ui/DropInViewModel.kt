/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.getPaymentMethods
import com.adyen.checkout.core.common.getStoredPaymentMethods
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.CheckoutParamsFactory
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.internal.CheckoutControllerFactory
import com.adyen.checkout.core.sessions.internal.DefaultSessionStateStore
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.DefaultStoredPaymentMethodRemover
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.StoredPaymentMethodRemover
import com.adyen.checkout.dropin.internal.data.api.DisableTokenRepository
import com.adyen.checkout.dropin.internal.data.api.DisableTokenService
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.reflect.KClass

/**
 * @param coroutineScope Becomes the [viewModelScope], so it is cancelled when Drop-in is closed. It is passed in
 * because the dependencies that live as long as Drop-in, like its analytics manager, need it before this view model
 * exists.
 */
@Suppress("LongParameterList")
internal class DropInViewModel(
    private val input: DropInResultContract.Input,
    val checkoutParams: CheckoutParams,
    val navigator: DropInNavigator,
    val controllerProvider: DropInControllerProvider,
    private val dropInServiceManager: DropInServiceManager,
    private val storedPaymentMethodRemover: StoredPaymentMethodRemover,
    val analyticsManager: AnalyticsManager,
    coroutineScope: CoroutineScope,
) : ViewModel(coroutineScope) {

    val dropInParams: DropInParams = DropInParamsMapper().mapToParams(checkoutParams)

    lateinit var paymentMethodRepository: PaymentMethodRepository

    val resultFlow: Flow<DropInResult> = merge(
        dropInServiceManager.paymentResultFlow.map { DropInResult.Completed(it) },
        dropInServiceManager.errorFlow.map { DropInResult.Failed(it.message ?: "Something went wrong") },
        navigator.finishFlow.filter { it }.map { DropInResult.Cancelled() },
    )

    init {
        initializePaymentMethods()
        initializeBackStack()
    }

    private fun initializePaymentMethods() {
        paymentMethodRepository = DefaultPaymentMethodRepository(
            paymentMethods = input.checkoutContext.getPaymentMethods(),
            storedPaymentMethods = input.checkoutContext.getStoredPaymentMethods(),
            storedPaymentMethodRemover = storedPaymentMethodRemover,
            // Removals outlive the management screen, so that leaving it can't cancel one halfway through.
            coroutineScope = viewModelScope,
        )
    }

    private fun initializeBackStack() {
        if (navigator.didRestoreState) return

        val storedPaymentMethods = paymentMethodRepository.storedPaymentMethods.value
        // Only startWithLastStoredPaymentMethod is read here. hideStoredPaymentMethods takes stored payment methods
        // off the list, which is a separate decision from what Drop-in opens on.
        val startingPoint = if (dropInParams.startWithLastStoredPaymentMethod && storedPaymentMethods.isNotEmpty()) {
            PreselectedPaymentMethodNavKey(storedPaymentMethods.first().id)
        } else {
            PaymentMethodListNavKey
        }
        navigator.navigateTo(startingPoint)
    }

    fun startDropInService(context: Context) {
        dropInServiceManager.start(context)
    }

    fun stopDropInService(context: Context) {
        dropInServiceManager.stop(context)
    }

    /**
     * @param analyticsManagerFactory Called once per Drop-in session. The manager it returns is shared by every
     * controller Drop-in creates, so the session makes a single analytics setup call.
     */
    class Factory(
        private val inputProvider: () -> DropInResultContract.Input,
        private val analyticsManagerFactory: (CheckoutContext, CheckoutParams, CoroutineScope) -> AnalyticsManager =
            ::createDropInAnalyticsManager,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return create(inputProvider(), extras.createSavedStateHandle()) as T
        }

        @VisibleForTesting
        internal fun create(input: DropInResultContract.Input, savedStateHandle: SavedStateHandle): DropInViewModel {
            val checkoutParams = CheckoutParamsFactory().create(input.checkoutContext)
            val dropInServiceManager = DropInServiceManager(input.serviceClass)
            val coroutineScope = CoroutineScope(SupervisorJob() + DispatcherProvider.Main.immediate)
            // TODO - Send the Drop-in level rendered and closed events through this manager, like v5's DropInEvents
            val analyticsManager = analyticsManagerFactory(input.checkoutContext, checkoutParams, coroutineScope)

            return DropInViewModel(
                input = input,
                checkoutParams = checkoutParams,
                navigator = DropInNavigator(
                    backStackPersister = SavedStateBackStackPersister(savedStateHandle = savedStateHandle),
                ),
                controllerProvider = DefaultDropInControllerProvider(
                    checkoutContext = input.checkoutContext,
                    dropInServiceManager = dropInServiceManager,
                    controllerFactory = CheckoutControllerFactory(analyticsManager),
                ),
                dropInServiceManager = dropInServiceManager,
                storedPaymentMethodRemover = createStoredPaymentMethodRemover(input.checkoutContext),
                analyticsManager = analyticsManager,
                coroutineScope = coroutineScope,
            )
        }

        private fun createStoredPaymentMethodRemover(checkoutContext: CheckoutContext): StoredPaymentMethodRemover {
            val httpClient = HttpClientFactory.getHttpClient(checkoutContext.checkoutConfiguration.environment)
            return DefaultStoredPaymentMethodRemover(
                checkoutContext = checkoutContext,
                disableTokenRepository = DisableTokenRepository(
                    disableTokenService = DisableTokenService(httpClient),
                    sessionStateStore = DefaultSessionStateStore,
                ),
            )
        }
    }
}

private fun createDropInAnalyticsManager(
    checkoutContext: CheckoutContext,
    checkoutParams: CheckoutParams,
    coroutineScope: CoroutineScope,
): AnalyticsManager {
    return AnalyticsManagerFactory().provide(
        context = checkoutContext,
        params = checkoutParams,
        source = checkoutContext.dropInAnalyticsSource(),
        coroutineScope = coroutineScope,
    )
}
