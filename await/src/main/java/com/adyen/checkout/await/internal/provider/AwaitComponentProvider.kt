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
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.internal.ActionComponentCallback
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.components.core.internal.data.api.StatusService
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.internal.data.api.HttpClientFactory

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AwaitComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams, overrideSessionParams)

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
        val componentParams = componentParamsMapper.mapToParams(configuration, null)
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
        private val PAYMENT_METHODS = listOf(
            PaymentMethodTypes.BLIK,
            PaymentMethodTypes.MB_WAY,
            PaymentMethodTypes.UPI_COLLECT,
        )
    }
}
