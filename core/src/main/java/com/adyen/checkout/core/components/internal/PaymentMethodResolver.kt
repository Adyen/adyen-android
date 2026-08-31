/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.getPaymentMethods
import com.adyen.checkout.core.common.getStoredPaymentMethods
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse

internal object PaymentMethodResolver {

    fun resolve(
        target: CheckoutTarget,
        context: CheckoutContext,
    ): PaymentMethodResponse? {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> resolvePaymentMethod(
                target = target,
                context = context,
            )

            is CheckoutTarget.StoredPaymentMethod -> resolveStoredPaymentMethod(
                target = target,
                context = context,
            )

            else -> {
                adyenLog(AdyenLogLevel.WARN) { "Invalid target: $target" }
                null
            }
        }
    }

    private fun resolvePaymentMethod(
        target: CheckoutTarget.PaymentMethod,
        context: CheckoutContext,
    ): PaymentMethodResponse? = context.getPaymentMethods().find { it.type == target.type }

    private fun resolveStoredPaymentMethod(
        target: CheckoutTarget.StoredPaymentMethod,
        context: CheckoutContext,
    ): PaymentMethodResponse? = context.getStoredPaymentMethods().find { it.id == target.id }
}
