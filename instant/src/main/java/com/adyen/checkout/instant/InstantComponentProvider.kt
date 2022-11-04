/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException

class InstantComponentProvider : PaymentComponentProvider<InstantComponent, InstantConfiguration> {
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: InstantConfiguration,
        defaultArgs: Bundle?,
        key: String?
    ): InstantComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                InstantComponent(
                    savedStateHandle = savedStateHandle,
                    delegate = DefaultInstantDelegate(
                        observerRepository = PaymentObserverRepository(),
                        paymentMethod = paymentMethod,
                        configuration = configuration
                    ),
                    configuration = configuration
                )
            }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, InstantComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return InstantComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
