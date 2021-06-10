/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.await

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.util.PaymentMethodTypes

private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BLIK, PaymentMethodTypes.MB_WAY)

class AwaitComponentProvider : ActionComponentProvider<AwaitComponent, AwaitConfiguration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: AwaitConfiguration
    ): AwaitComponent {
        val awaitFactory = viewModelFactory {
            AwaitComponent(
                application,
                configuration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, awaitFactory).get(AwaitComponent::class.java)
    }

    override fun requiresConfiguration(): Boolean = true

    override fun getSupportedActionTypes(): List<String> {
        return listOf(AwaitAction.ACTION_TYPE)
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun requiresView(action: Action): Boolean = true
}
