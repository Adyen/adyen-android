/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/5/2021.
 */

package com.adyen.checkout.redirect

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.redirect.handler.DefaultRedirectHandler

class RedirectComponentProvider : ActionComponentProvider<RedirectComponent, RedirectConfiguration, RedirectDelegate> {
    override fun <T> get(
        owner: T,
        application: Application,
        configuration: RedirectConfiguration
    ): RedirectComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: RedirectConfiguration,
        defaultArgs: Bundle?
    ): RedirectComponent {
        val redirectFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val redirectDelegate = getDelegate(configuration, savedStateHandle, application)
            RedirectComponent(
                savedStateHandle,
                application,
                configuration,
                redirectDelegate
            )
        }
        return ViewModelProvider(viewModelStoreOwner, redirectFactory).get(RedirectComponent::class.java)
    }

    override fun getDelegate(
        configuration: RedirectConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): RedirectDelegate {
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        return DefaultRedirectDelegate(redirectHandler, paymentDataRepository)
    }

    override val supportedActionTypes: List<String>
        get() = listOf(RedirectAction.ACTION_TYPE)

    @Deprecated(
        message = "You can safely remove this method, it will always return true as all action components require " +
            "a configuration.",
        replaceWith = ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun requiresView(action: Action): Boolean = false

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun providesDetails(): Boolean {
        return true
    }
}
