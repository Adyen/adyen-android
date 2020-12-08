/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/12/2020.
 */

package com.adyen.checkout.base.component

import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes

/**
 * A generic delegate for a StoredPaymentMethod
 */
class GenericStoredPaymentDelegate(val storedPaymentMethod: StoredPaymentMethod) : StoredPaymentMethodDelegate {

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }
}