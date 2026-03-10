/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/3/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

internal class PaymentMethodSupportCheck {

    fun isSupported(paymentMethod: PaymentMethod): Boolean {
        // Only checks if the payment method type is explicitly not supported. This allows unknown redirect payment
        // methods to be supported.
        return !PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type)
    }

    fun isSupported(paymentMethod: StoredPaymentMethod): Boolean {
        return paymentMethod.id.isNotEmpty() &&
            PaymentMethodTypes.SUPPORTED_STORED_PAYMENT_METHODS.contains(paymentMethod.type) &&
            paymentMethod.supportedShopperInteractions.contains(SHOPPER_INTERACTION_ECOMMERCE)
    }

    companion object {
        private const val SHOPPER_INTERACTION_ECOMMERCE = "Ecommerce"
    }
}
