/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.molpay

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.issuerlist.DefaultIssuerListDelegate

class MolpayComponentProvider : PaymentComponentProvider<MolpayComponent, MolpayConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: MolpayConfiguration,
        defaultArgs: Bundle?
    ): MolpayComponent {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate = DefaultIssuerListDelegate(paymentMethod) { MolpayPaymentMethod() }
                MolpayComponent(savedStateHandle, delegate, configuration)
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory).get(MolpayComponent::class.java)
    }
}
