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
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.issuerlist.DefaultIssuerListDelegate

class MolpayComponentProvider : PaymentComponentProvider<MolpayComponent, MolpayConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: MolpayConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): MolpayComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate = DefaultIssuerListDelegate(configuration, paymentMethod) { MolpayPaymentMethod() }
                MolpayComponent(savedStateHandle, delegate, configuration)
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, MolpayComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return MolpayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
