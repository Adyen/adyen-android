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
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter

class GiftCardComponentProvider : PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: GiftCardConfiguration,
        defaultArgs: Bundle?
    ): GiftCardComponent {
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            GiftCardComponent(
                savedStateHandle = savedStateHandle,
                giftCardDelegate = DefaultGiftCardDelegate(
                    paymentMethod,
                    DefaultPublicKeyRepository(),
                    configuration,
                    cardEncrypter
                ),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, giftCardFactory).get(GiftCardComponent::class.java)
    }
}
