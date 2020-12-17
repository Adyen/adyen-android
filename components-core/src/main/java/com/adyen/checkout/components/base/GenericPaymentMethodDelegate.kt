/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.components.base

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes

/**
 * A generic delegate for a regular PaymentMethod
 */
class GenericPaymentMethodDelegate(val paymentMethod: PaymentMethod) : PaymentMethodDelegate {
    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }
}
