/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository

class BcmcComponentProvider : PaymentComponentProvider<BcmcComponent, BcmcConfiguration> {
    override operator fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration
    ): BcmcComponent {
        val publicKeyRepository = PublicKeyRepository()
        val bcmcFactory = viewModelFactory {
            BcmcComponent(
                GenericPaymentMethodDelegate(paymentMethod),
                configuration,
                publicKeyRepository
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory).get(BcmcComponent::class.java)
    }
}
