/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.action

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.repository.ActionObserverRepository

class GenericActionComponentProvider :
    ActionComponentProvider<GenericActionComponent, GenericActionConfiguration, GenericActionDelegate> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: GenericActionConfiguration,
        key: String?,
    ): GenericActionComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null, key)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: GenericActionConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): GenericActionComponent {
        val genericActionFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val genericActionDelegate = getDelegate(configuration, savedStateHandle, application)
            GenericActionComponent(
                configuration,
                genericActionDelegate
            )
        }
        return ViewModelProvider(viewModelStoreOwner, genericActionFactory)[key, GenericActionComponent::class.java]
    }

    override fun getDelegate(
        configuration: GenericActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application
    ): GenericActionDelegate {
        return DefaultGenericActionDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            configuration = configuration,
            actionDelegateProvider = ActionDelegateProvider()
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
