/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.example.ui.v6

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.RegularPaymentMethodResponse
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private val SUPPORTED_PAYMENT_METHODS = setOf(
    PaymentMethodTypes.BLIK,
    PaymentMethodTypes.GOOGLE_PAY,
    PaymentMethodTypes.GOOGLE_PAY_LEGACY,
    PaymentMethodTypes.MB_WAY,
    PaymentMethodTypes.SCHEME,
)

internal fun List<RegularPaymentMethodResponse>.filterSupportedPaymentMethods(): List<RegularPaymentMethodResponse> =
    filter { it.type in SUPPORTED_PAYMENT_METHODS || it is GenericPaymentMethodResponse }

internal suspend fun List<RegularPaymentMethodResponse>.filterAvailablePaymentMethods(
    context: CheckoutContext,
): List<RegularPaymentMethodResponse> = coroutineScope {
    map { paymentMethod ->
        async { paymentMethod to Checkout.isPaymentMethodAvailable(paymentMethod.type, context) }
    }
        .awaitAll()
        .filter { it.second }
        .map { it.first }
}
