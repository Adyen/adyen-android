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

/**
 * The action screen of a payment flow. [owner] says which view model holds the controller that continues it, which is
 * also what the entry scopes its view model store to.
 */
// TODO - Prototype: an action cannot be restored after process death. A persisted key currently renders an empty
//  screen; it should be truncated back to the starting point instead.
@Serializable
internal data class ActionNavKey(
    val paymentFlowType: DropInPaymentFlowType,
    val owner: ActionFlowOwner,
) : NavKey

/**
 * The view model that owns a payment flow, and therefore the controller its action screen continues on.
 */
@Serializable
internal enum class ActionFlowOwner {

    /** [PaymentMethodViewModel], for a payment method with a screen of its own. */
    PAYMENT_METHOD,

    /** [PaymentMethodListViewModel], for an express payment method rendered on the list. */
    EXPRESS_PAYMENT_METHOD,
}

/**
 * The flow key the action of [ActionNavKey] shares with the screens it continues from, so that it resolves the view
 * model those screens created rather than a fresh one.
 */
internal fun ActionNavKey.flowKey(): String = when (owner) {
    ActionFlowOwner.PAYMENT_METHOD -> paymentFlowKey(paymentFlowType)
    ActionFlowOwner.EXPRESS_PAYMENT_METHOD -> EXPRESS_PAYMENT_METHOD_FLOW_KEY
}

/**
 * The nav entries of one payment flow all declare this key through [scopeTo], which gives them a single view model
 * store that is cleared once the last of them leaves the back stack.
 *
 * That is what scopes [PaymentMethodViewModel] to the flow instead of to a single screen.
 */
internal fun paymentFlowKey(paymentFlowType: DropInPaymentFlowType): String =
    "payment-flow-$paymentFlowType"

/**
 * The flow key of the express payment methods, declared by [PaymentMethodListNavKey] and by an [ActionNavKey] owned
 * by [ActionFlowOwner.EXPRESS_PAYMENT_METHOD].
 *
 * The list hosts their buttons, so their controllers are owned by [PaymentMethodListViewModel]. Sharing this key with
 * the action screen keeps that view model alive when the list is replaced, so the flow carries on with the controller
 * that started it.
 */
internal const val EXPRESS_PAYMENT_METHOD_FLOW_KEY = "express-payment-method-flow"
