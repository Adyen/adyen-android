/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.await

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.ActionComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultActionComponentEventHandler
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.DefaultStatusRepository
import com.adyen.checkout.components.status.api.StatusService
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.HttpClientFactory

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AwaitComponentProvider(
    overrideComponentParams: ComponentParams? = null
) : ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams)

    override val supportedActionTypes: List<String>
        get() = listOf(AwaitAction.ACTION_TYPE)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: AwaitConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): AwaitComponent {
        val awaitFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val awaitDelegate = getDelegate(configuration, savedStateHandle, application)
            AwaitComponent(
                awaitDelegate,
                DefaultActionComponentEventHandler(callback)
            )
        }
        return ViewModelProvider(viewModelStoreOwner, awaitFactory)[key, AwaitComponent::class.java].also { component ->
            component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
        }
    }

    override fun getDelegate(
        configuration: AwaitConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): AwaitDelegate {
        val componentParams = componentParamsMapper.mapToParams(configuration)
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, configuration.clientKey)
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        return DefaultAwaitDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = componentParams,
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository
        )
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }

    companion object {
        private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BLIK, PaymentMethodTypes.MB_WAY)
    }
}
