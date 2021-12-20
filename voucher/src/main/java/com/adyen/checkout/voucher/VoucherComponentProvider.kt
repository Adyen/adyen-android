/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.util.PaymentMethodTypes

private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BACS)

class VoucherComponentProvider : ActionComponentProvider<VoucherComponent, VoucherConfiguration> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: VoucherConfiguration
    ): VoucherComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: VoucherConfiguration,
        defaultArgs: Bundle?
    ): VoucherComponent {
        val voucherFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            VoucherComponent(
                savedStateHandle,
                application,
                configuration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, voucherFactory).get(VoucherComponent::class.java)
    }

    @Deprecated(
        "You can safely remove this method, it will always return true as all action components require a configuration.",
        ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun requiresView(action: Action): Boolean {
        return true
    }

    override fun getSupportedActionTypes(): List<String> {
        return listOf(VoucherAction.ACTION_TYPE)
    }

    override fun providesDetails(): Boolean {
        return false
    }
}
