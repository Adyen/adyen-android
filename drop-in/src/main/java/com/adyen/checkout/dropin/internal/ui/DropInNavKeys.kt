/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object EmptyNavKey : NavKey

@Serializable
internal data class PreselectedPaymentMethodNavKey(
    val storedPaymentMethodId: String,
) : NavKey

@Serializable
internal data object PaymentMethodListNavKey : NavKey

@Serializable
internal data object StoredPaymentMethodsNavKey : NavKey

@Serializable
internal data class PaymentMethodNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : NavKey

// TODO - A payment flow cannot be restored after process death. No action state is persisted and the controller is
//  rebuilt from scratch, so this key renders a screen that never progresses. The back stack should be truncated back
//  to the starting point instead. To be solved as part of process death handling.
@Serializable
internal data class ActionNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : NavKey
