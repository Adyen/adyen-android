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

/**
 * The screen of the payment method itself. A payment method that takes no input from the shopper renders on this same
 * key, reporting the progress of the payments call instead of a component.
 */
@Serializable
internal data class PaymentMethodNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : NavKey

// TODO - Prototype: an action cannot be restored after process death. A persisted key currently renders an empty
//  screen; it should be truncated back to the starting point instead.
@Serializable
internal data class ActionNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : NavKey

/**
 * The action screen of the Google Pay flow, which is separate from [ActionNavKey] because that flow is owned by
 * [GooglePayViewModel] rather than by [PaymentMethodViewModel].
 */
@Serializable
internal data class GooglePayActionNavKey(
    val paymentFlowType: DropInPaymentFlowType,
) : NavKey

/**
 * The nav entries of one payment flow all declare this content key. Nav3 treats entries sharing a content key as
 * sharing their [androidx.navigation3.runtime.NavEntryDecorator] state, which gives them one view model store, and
 * only clears it once the last of those entries left the back stack.
 *
 * That is what scopes [PaymentMethodViewModel] to the flow instead of to a single screen.
 */
internal fun paymentFlowContentKey(paymentFlowType: DropInPaymentFlowType): String =
    "payment-flow-$paymentFlowType"

/**
 * The content key of the Google Pay flow, declared by [PaymentMethodListNavKey] and [GooglePayActionNavKey].
 *
 * The list hosts the Google Pay button, so it is where [GooglePayViewModel] is created. Sharing this key with the
 * action screen keeps that view model alive when the list is replaced, so the flow carries on with the controller that
 * started it. It also means the list keeps its own view models around until the action is done, which is harmless
 * because the whole store is cleared as soon as neither entry is on the back stack.
 */
internal const val GOOGLE_PAY_FLOW_CONTENT_KEY = "google-pay-flow"
