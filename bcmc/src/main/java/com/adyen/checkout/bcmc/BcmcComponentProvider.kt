/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository

class BcmcComponentProvider : PaymentComponentProvider<BcmcComponent, BcmcConfiguration> {
    override fun <T> get(
        owner: T,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration
    ): BcmcComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, paymentMethod, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration,
        defaultArgs: Bundle?
    ): BcmcComponent {
        val publicKeyRepository = PublicKeyRepository()
        val cardValidationMapper = CardValidationMapper()
        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            BcmcComponent(
                savedStateHandle,
                GenericPaymentMethodDelegate(paymentMethod),
                configuration,
                publicKeyRepository,
                cardValidationMapper
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory).get(BcmcComponent::class.java)
    }
}
