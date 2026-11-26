/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.getPaymentMethods
import com.adyen.checkout.core.common.getStoredPaymentMethods
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutLogLevel
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.RegularPaymentMethodResponse

internal object PaymentMethodResolver {

    fun resolve(
        target: CheckoutTarget,
        context: CheckoutContext,
    ): PaymentMethodResponse? {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> resolve(
                target = target,
                context = context,
            )

            is CheckoutTarget.StoredPaymentMethod -> resolveStoredPaymentMethod(
                target = target,
                context = context,
            )

            else -> {
                adyenLog(CheckoutLogLevel.WARN) { "Invalid target: $target" }
                null
            }
        }
    }

    fun resolve(
        target: CheckoutTarget.PaymentMethod,
        context: CheckoutContext,
    ): RegularPaymentMethodResponse? = context.getPaymentMethods().find { it.type == target.type }

    private fun resolveStoredPaymentMethod(
        target: CheckoutTarget.StoredPaymentMethod,
        context: CheckoutContext,
    ): PaymentMethodResponse? = context.getStoredPaymentMethods().find { it.id == target.id }
}
