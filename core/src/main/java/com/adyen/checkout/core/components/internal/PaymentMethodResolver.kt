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
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods

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
    ): PaymentMethodResponse? {
        val paymentMethods = context.getPaymentMethodResponse()?.paymentMethods
        return paymentMethods?.find { it.type == target.type }
    }

    private fun resolveStoredPaymentMethod(
        target: CheckoutTarget.StoredPaymentMethod,
        context: CheckoutContext,
    ): PaymentMethodResponse? {
        val storedPaymentMethods = context.getPaymentMethodResponse()?.storedPaymentMethods
        return storedPaymentMethods?.find { it.id == target.id }
    }

    private fun CheckoutContext.getPaymentMethodResponse(): PaymentMethods? {
        return when (this) {
            is CheckoutContext.Advanced -> paymentMethods
            is CheckoutContext.Sessions -> checkoutSession.sessionSetupResponse.paymentMethods
            is CheckoutContext.ActionOnly -> null
        }
    }
}
