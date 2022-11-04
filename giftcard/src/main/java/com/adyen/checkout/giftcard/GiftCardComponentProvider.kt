/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter

class GiftCardComponentProvider : PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: GiftCardConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): GiftCardComponent {
        assertSupported(paymentMethod)

        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            GiftCardComponent(
                savedStateHandle = savedStateHandle,
                delegate = DefaultGiftCardDelegate(
                    observerRepository = PaymentObserverRepository(),
                    paymentMethod = paymentMethod,
                    publicKeyRepository = DefaultPublicKeyRepository(),
                    configuration = configuration,
                    cardEncrypter = cardEncrypter
                ),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, GiftCardComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return GiftCardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
