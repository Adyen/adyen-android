/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingPLPaymentMethod
import com.adyen.checkout.components.repository.ObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.issuerlist.DefaultIssuerListDelegate

class OnlineBankingPLComponentProvider :
    PaymentComponentProvider<OnlineBankingPLComponent, OnlineBankingPLConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingPLConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): OnlineBankingPLComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate = DefaultIssuerListDelegate(
                    observerRepository = ObserverRepository(),
                    configuration = configuration,
                    paymentMethod = paymentMethod
                ) { OnlineBankingPLPaymentMethod() }
                OnlineBankingPLComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingPLComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return OnlineBankingPLComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
