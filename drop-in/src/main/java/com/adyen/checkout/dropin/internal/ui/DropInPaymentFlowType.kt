/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import kotlinx.serialization.Serializable

@Serializable
internal sealed class DropInPaymentFlowType {
    data class RegularPaymentMethod(val txVariant: String) : DropInPaymentFlowType()
    data class StoredPaymentMethod(val id: String) : DropInPaymentFlowType()
}
