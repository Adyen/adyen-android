/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.await.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.await.AwaitComponent
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.await.internal.ui.AwaitDelegate
import com.adyen.checkout.await.internal.ui.DefaultAwaitDelegate
import com.adyen.checkout.await.toCheckoutConfiguration
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.components.core.internal.data.api.StatusService
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.old.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.old.internal.util.LocaleProvider
import com.adyen.checkout.ui.core.internal.DefaultRedirectHandler

class AwaitComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val analyticsManager: AnalyticsManager? = null,
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) : ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> {

    override val supportedActionTypes: List<String>
        get() = listOf(AwaitAction.ACTION_TYPE)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String?
    ): AwaitComponent {
        val awaitFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val awaitDelegate = getDelegate(checkoutConfiguration, savedStateHandle, application)
            AwaitComponent(
                awaitDelegate,
                DefaultActionComponentEventHandler(),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, awaitFactory)[key, AwaitComponent::class.java].also { component ->
            component.observe(lifecycleOwner) {
                component.actionComponentEventHandler.onActionComponentEvent(it, callback)
            }
        }
    }

    override fun getDelegate(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application
    ): AwaitDelegate {
        val componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = localeProvider.getLocale(application),
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
        )

        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, componentParams.clientKey)
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        return DefaultAwaitDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = componentParams,
            redirectHandler = redirectHandler,
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
            analyticsManager = analyticsManager,
        )
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: AwaitConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): AwaitComponent {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            application = application,
            checkoutConfiguration = configuration.toCheckoutConfiguration(),
            callback = callback,
            key = key,
        )
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }

    companion object {
        private val PAYMENT_METHODS = listOf(
            PaymentMethodTypes.BLIK,
            PaymentMethodTypes.MB_WAY,
            PaymentMethodTypes.PAY_TO,
            PaymentMethodTypes.UPI_COLLECT,
            PaymentMethodTypes.UPI_INTENT,
        )
    }
}
