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

/**
 * Marks the keys that are part of an active payment flow. The flow, and therefore its controller, is kept alive as
 * long as at least one key of this type is on the back stack.
 */
internal sealed interface PaymentFlowNavKey : NavKey

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
) : PaymentFlowNavKey

/**
 * The screen for a payment method that takes no input from the shopper. It only reports that the payments call is
 * running; the flow continues on the action screen once that call returns an action.
 */
@Serializable
internal data class NoUiPaymentMethodNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : PaymentFlowNavKey

// TODO - Prototype: an action cannot be restored after process death. A persisted key currently renders an empty
//  screen; it should be truncated back to the starting point instead.
@Serializable
internal data object ActionNavKey : PaymentFlowNavKey
