/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.Order

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
