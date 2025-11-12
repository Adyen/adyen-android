/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.example.ui.v6

import com.adyen.checkout.core.components.CheckoutContext
import com.adyen.checkout.core.components.data.model.PaymentMethod

@Suppress("RestrictedApi")
internal fun CheckoutContext.getPaymentMethods(): List<PaymentMethod> {
    val paymentMethods = when (this) {
        is CheckoutContext.Advanced -> this.paymentMethodsApiResponse.paymentMethods
        is CheckoutContext.Sessions ->
            this.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse?.paymentMethods
    }

    return paymentMethods.orEmpty()
}
