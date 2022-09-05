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
import com.adyen.checkout.components.base.ActionDelegate
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

class GenericActionComponentProvider :
    ActionComponentProvider<GenericActionComponent, GenericActionConfiguration, ActionDelegate<*>> {
    override fun <T> get(
        owner: T,
        application: Application,
        configuration: GenericActionConfiguration
    ): GenericActionComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: GenericActionConfiguration,
        defaultArgs: Bundle?
    ): GenericActionComponent {
        val genericActionFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            GenericActionComponent(
                savedStateHandle,
                application,
                configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, genericActionFactory).get(GenericActionComponent::class.java)
    }

    override fun getDelegate(
        configuration: GenericActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): ActionDelegate<*> {
        throw IllegalStateException("GenericActionComponent doesn't have a delegate")
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

    @Deprecated(
        message = "You can safely remove this method, it will always return true as all action components require " +
            "a configuration.",
        replaceWith = ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun requiresView(action: Action): Boolean = true

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun providesDetails(): Boolean {
        return true
    }
}
