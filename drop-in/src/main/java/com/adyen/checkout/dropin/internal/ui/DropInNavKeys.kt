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

/**
 * The list hosts the promoted payment method buttons, so their controllers are owned by its view model and this entry
 * is the parent their action screen declares.
 */
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
 * also the entry this one declares as its parent.
 */
// TODO - Prototype: a payment flow cannot be restored after process death. No action state is persisted and the
//  controller is rebuilt from scratch, so this key renders an empty screen. Worse, when the payment method
//  takes no shopper input, the rebuilt PaymentMethodViewModel submits the payment a second time: the guard in
//  FullCheckoutFlow is an in-memory AtomicBoolean, so it resets with the process. A restored PaymentMethodNavKey
//  submits twice for the same reason. Both should be truncated back to the starting point instead.
@Serializable
internal data class ActionNavKey(
    val paymentFlowType: DropInPaymentFlowType,
    val owner: ActionFlowOwner,
) : NavKey {

    /**
     * The entry holding the view model that owns this flow, declared to the decorator through
     * [SharedViewModelStoreNavEntryDecorator.parent].
     *
     * Reaching into the parent's store rather than sharing one is what lets a controller outlive the screen that
     * created it, even though this screen replaces that screen on the back stack.
     */
    val parentContentKey: Any get() = when (owner) {
        ActionFlowOwner.PAYMENT_METHOD -> PaymentMethodNavKey(paymentFlowType).toContentKey()
        ActionFlowOwner.PAYMENT_METHOD_LIST -> PaymentMethodListNavKey.toContentKey()
    }
}

/**
 * The view model that owns a payment flow, and therefore the controller its action screen continues on.
 */
@Serializable
internal enum class ActionFlowOwner {

    /** [PaymentMethodViewModel], for a payment method with a screen of its own. */
    PAYMENT_METHOD,

    /** [PaymentMethodListViewModel], for a promoted payment method rendered on the list. */
    PAYMENT_METHOD_LIST,
}
