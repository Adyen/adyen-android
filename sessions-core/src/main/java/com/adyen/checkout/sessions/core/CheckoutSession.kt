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

/**
 * A class holding the data required to launch Drop-in or a component with the sessions flow.
 * Use [CheckoutSessionProvider.createSession] to create this class.
 */
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    val order: Order?,
) {
    fun getPaymentMethod(paymentMethodType: String): PaymentMethod? {
        return sessionSetupResponse.paymentMethodsApiResponse?.paymentMethods.orEmpty().firstOrNull {
            it.type == paymentMethodType
        }
    }
}