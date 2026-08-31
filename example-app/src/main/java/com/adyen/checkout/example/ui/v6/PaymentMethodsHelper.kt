/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.example.ui.v6

import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

private val SUPPORTED_PAYMENT_METHODS = setOf(
    PaymentMethodTypes.BLIK,
    PaymentMethodTypes.GOOGLE_PAY,
    PaymentMethodTypes.GOOGLE_PAY_LEGACY,
    PaymentMethodTypes.MB_WAY,
    PaymentMethodTypes.SCHEME,
)

internal fun List<PaymentMethod>.filterSupportedPaymentMethods(): List<PaymentMethod> =
    filter { it.type in SUPPORTED_PAYMENT_METHODS || it is GenericPaymentMethod }
