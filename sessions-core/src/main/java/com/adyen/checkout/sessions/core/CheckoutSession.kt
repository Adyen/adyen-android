/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod

// TODO docs
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    val order: Order?,
) {
    fun getPaymentMethod(paymentMethodType: String): PaymentMethod? {
        return sessionSetupResponse.paymentMethods?.paymentMethods.orEmpty().firstOrNull {
            it.type == paymentMethodType
        }
    }
}
