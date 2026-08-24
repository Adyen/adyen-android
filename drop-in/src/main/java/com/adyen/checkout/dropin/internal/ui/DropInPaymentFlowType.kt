/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import kotlinx.serialization.Serializable

@Serializable
internal sealed class DropInPaymentFlowType {

    @Serializable
    data class RegularPaymentMethod(val txVariant: String) : DropInPaymentFlowType()

    @Serializable
    data class StoredPaymentMethod(val id: String) : DropInPaymentFlowType()
}

internal fun PaymentMethodRepository.findPaymentMethod(
    paymentFlowType: DropInPaymentFlowType,
): PaymentMethodResponse = when (paymentFlowType) {
    is DropInPaymentFlowType.RegularPaymentMethod -> {
        paymentMethods.first { it.type == paymentFlowType.txVariant }
    }

    is DropInPaymentFlowType.StoredPaymentMethod -> {
        storedPaymentMethods.value.first { it.id == paymentFlowType.id }
    }
}
