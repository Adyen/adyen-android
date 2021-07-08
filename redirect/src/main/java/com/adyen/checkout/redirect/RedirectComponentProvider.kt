/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/5/2021.
 */

package com.adyen.checkout.redirect

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.RedirectAction

class RedirectComponentProvider : ActionComponentProvider<RedirectComponent, RedirectConfiguration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: RedirectConfiguration
    ): RedirectComponent {
        val redirectDelegate = RedirectDelegate()
        val redirectFactory = viewModelFactory {
            RedirectComponent(
                application,
                configuration,
                redirectDelegate
            )
        }
        return ViewModelProvider(viewModelStoreOwner, redirectFactory).get(RedirectComponent::class.java)
    }

    override fun requiresConfiguration(): Boolean = false

    override fun requiresView(action: Action): Boolean = false

    override fun getSupportedActionTypes(): List<String> {
        return listOf(RedirectAction.ACTION_TYPE)
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }
}
