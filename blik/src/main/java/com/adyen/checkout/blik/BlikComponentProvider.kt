/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.blik

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.exception.ComponentException

class BlikComponentProvider : StoredPaymentComponentProvider<BlikComponent, BlikConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BlikConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): BlikComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                BlikComponent(
                    savedStateHandle = savedStateHandle,
                    delegate = DefaultBlikDelegate(configuration, paymentMethod),
                    configuration = configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BlikComponent::class.java]
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: BlikConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): BlikComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                BlikComponent(
                    savedStateHandle = savedStateHandle,
                    delegate = StoredBlikDelegate(configuration, storedPaymentMethod),
                    configuration = configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, BlikComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    private fun assertSupported(storedPaymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(storedPaymentMethod)) {
            throw ComponentException("Unsupported payment method ${storedPaymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BlikComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return BlikComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
