/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import kotlinx.serialization.Serializable

/**
 * Google Pay is rendered by its own component on the payment method list rather than behind a list item, so both of
 * its types are handled separately from the other payment methods.
 */
internal val GOOGLE_PAY_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)

@Serializable
internal sealed class DropInPaymentFlowType {

    @Serializable
    data class RegularPaymentMethod(val txVariant: String) : DropInPaymentFlowType()

    @Serializable
    data class StoredPaymentMethod(val id: String) : DropInPaymentFlowType()
}
