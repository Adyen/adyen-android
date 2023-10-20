/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.twint.TwintActionComponent
import com.adyen.checkout.twint.TwintActionConfiguration
import com.adyen.checkout.twint.internal.ui.DefaultTwintDelegate
import com.adyen.checkout.twint.internal.ui.TwintDelegate

class TwintActionComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : ActionComponentProvider<TwintActionComponent, TwintActionConfiguration, TwintDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams, overrideSessionParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: TwintActionConfiguration,
        callback: ActionComponentCallback,
        key     : String?,
    ): TwintActionComponent {
        val twintFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val twintDelegate = getDelegate(configuration, savedStateHandle, application)
            TwintActionComponent(
                delegate = twintDelegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback)
            )
        }

        return ViewModelProvider(viewModelStoreOwner, twintFactory)[key, TwintActionComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        configuration: TwintActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): TwintDelegate {
        val componentParams = componentParamsMapper.mapToParams(configuration, null)
        return DefaultTwintDelegate(
            observerRepository = ActionObserverRepository(),
            componentParams = componentParams,
            paymentDataRepository = PaymentDataRepository(savedStateHandle),
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
