/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.getPaymentMethods
import com.adyen.checkout.core.common.getStoredPaymentMethods
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.CheckoutParamsFactory
import com.adyen.checkout.core.common.internal.publicKey
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.reflect.KClass

internal class DropInViewModel(
    private val input: DropInResultContract.Input,
    val navigator: DropInNavigator,
    val controllerProvider: DropInControllerProvider,
    private val dropInServiceManager: DropInServiceManager,
) : ViewModel() {

    val checkoutParams: CheckoutParams = createCheckoutParams()

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
        )
    }

    private fun createCheckoutParams(): CheckoutParams {
        return CheckoutParamsFactory().create(
            configuration = input.checkoutContext.checkoutConfiguration,
            session = (input.checkoutContext as? CheckoutContext.Sessions)?.checkoutSession,
            publicKey = input.checkoutContext.publicKey,
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

    class Factory(
        private val inputProvider: () -> DropInResultContract.Input,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val input = inputProvider()
            val dropInServiceManager = DropInServiceManager(input.serviceClass)

            return DropInViewModel(
                input = input,
                navigator = DropInNavigator(
                    backStackPersister = SavedStateBackStackPersister(
                        savedStateHandle = extras.createSavedStateHandle(),
                    ),
                ),
                controllerProvider = DefaultDropInControllerProvider(
                    checkoutContext = input.checkoutContext,
                    dropInServiceManager = dropInServiceManager,
                ),
                dropInServiceManager = dropInServiceManager,
            ) as T
        }
    }
}
