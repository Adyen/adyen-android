/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.action.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.SdkAction
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
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.twint.action.TwintActionComponent
import com.adyen.checkout.twint.action.TwintActionConfiguration
import com.adyen.checkout.twint.action.internal.ui.DefaultTwintActionDelegate
import com.adyen.checkout.twint.action.internal.ui.TwintActionDelegate
import com.adyen.checkout.twint.action.toCheckoutConfiguration

class TwintActionComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val analyticsManager: AnalyticsManager? = null,
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) : ActionComponentProvider<TwintActionComponent, TwintActionConfiguration, TwintActionDelegate> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String?
    ): TwintActionComponent {
        val twintFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val twintDelegate = getDelegate(checkoutConfiguration, savedStateHandle, application)
            TwintActionComponent(
                delegate = twintDelegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, twintFactory)[key, TwintActionComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner) {
                    component.actionComponentEventHandler.onActionComponentEvent(it, callback)
                }
            }
    }

    override fun getDelegate(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): TwintActionDelegate {
        val componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = localeProvider.getLocale(application),
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
        )

        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, componentParams.clientKey)

        return DefaultTwintActionDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = componentParams,
            paymentDataRepository = PaymentDataRepository(savedStateHandle),
            statusRepository = statusRepository,
            analyticsManager = analyticsManager,
        )
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: TwintActionConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): TwintActionComponent {
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

    override val supportedActionTypes: List<String> = listOf(SdkAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }

    companion object {
        private val PAYMENT_METHODS = listOf(PaymentMethodTypes.TWINT)
    }
}
