/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.issuerlist.DefaultIssuerListDelegate

class PayByBankComponentProvider : PaymentComponentProvider<PayByBankComponent, PayByBankConfiguration> {
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: PayByBankConfiguration,
        defaultArgs: Bundle?
    ): PayByBankComponent {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                PayByBankComponent(
                    savedStateHandle,
                    DefaultPayByBankDelegate(paymentMethod, configuration),
                    DefaultIssuerListDelegate(configuration, paymentMethod) { PayByBankPaymentMethod() },
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory).get(PayByBankComponent::class.java)
    }
}
