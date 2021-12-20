/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.await

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.util.PaymentMethodTypes

private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BLIK, PaymentMethodTypes.MB_WAY)

class AwaitComponentProvider : ActionComponentProvider<AwaitComponent, AwaitConfiguration> {
    override fun <T> get(
        owner: T,
        application: Application,
        configuration: AwaitConfiguration
    ): AwaitComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: AwaitConfiguration,
        defaultArgs: Bundle?
    ): AwaitComponent {
        val awaitFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            AwaitComponent(
                savedStateHandle,
                application,
                configuration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, awaitFactory).get(AwaitComponent::class.java)
    }

    @Deprecated(
        "You can safely remove this method, it will always return true as all action components require a configuration.",
        ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun getSupportedActionTypes(): List<String> {
        return listOf(AwaitAction.ACTION_TYPE)
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun requiresView(action: Action): Boolean = true

    override fun providesDetails(): Boolean {
        return true
    }
}
