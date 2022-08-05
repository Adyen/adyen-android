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
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.cse.DefaultCardEncrypter

class BcmcComponentProvider : PaymentComponentProvider<BcmcComponent, BcmcConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration,
        defaultArgs: Bundle?
    ): BcmcComponent {
        val publicKeyRepository = DefaultPublicKeyRepository()
        val cardValidationMapper = CardValidationMapper()
        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            BcmcComponent(
                savedStateHandle = savedStateHandle,
                paymentMethodDelegate = GenericPaymentMethodDelegate(paymentMethod),
                bcmcDelegate = DefaultBcmcDelegate(
                    paymentMethod,
                    publicKeyRepository,
                    configuration,
                    cardValidationMapper,
                    DefaultCardEncrypter()
                ),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory).get(BcmcComponent::class.java)
    }
}
