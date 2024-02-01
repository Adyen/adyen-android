/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.action.core.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.GenericActionComponent
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.ActionDelegateProvider
import com.adyen.checkout.action.core.internal.ui.DefaultGenericActionDelegate
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.action.core.toCheckoutConfiguration
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory

class GenericActionComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val isCreatedByDropIn: Boolean = false,
) : ActionComponentProvider<GenericActionComponent, GenericActionConfiguration, GenericActionDelegate> {

    private val componentParamsMapper = GenericComponentParamsMapper(isCreatedByDropIn, null)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String?
    ): GenericActionComponent {
        val genericActionFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val genericActionDelegate = getDelegate(checkoutConfiguration, savedStateHandle, application)
            GenericActionComponent(
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, null),
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, genericActionFactory)[key, GenericActionComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application
    ): GenericActionDelegate {
        val componentParams = componentParamsMapper.mapToParams(checkoutConfiguration, null)
        return DefaultGenericActionDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            checkoutConfiguration = checkoutConfiguration,
            componentParams = componentParams,
            actionDelegateProvider = ActionDelegateProvider(isCreatedByDropIn, null),
        )
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: GenericActionConfiguration,
        callback: ActionComponentCallback,
        key: String?,
    ): GenericActionComponent {
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

    override val supportedActionTypes: List<String>
        get() = listOf(
            AwaitAction.ACTION_TYPE,
            QrCodeAction.ACTION_TYPE,
            RedirectAction.ACTION_TYPE,
            Threeds2Action.ACTION_TYPE,
            Threeds2ChallengeAction.ACTION_TYPE,
            Threeds2FingerprintAction.ACTION_TYPE,
            VoucherAction.ACTION_TYPE,
            SdkAction.ACTION_TYPE,
        )

    override fun canHandleAction(action: Action): Boolean = getProvider(action).canHandleAction(action)

    override fun providesDetails(action: Action): Boolean = getProvider(action).providesDetails(action)

    private fun getProvider(action: Action): ActionComponentProvider<*, *, *> {
        return getActionProviderFor(action) ?: throw IllegalArgumentException("No provider available for this action")
    }
}
