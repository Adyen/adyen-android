package com.adyen.checkout.giftcard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class is used in [com.adyen.checkout.giftcard.internal.GiftCardComponentEventHandler] and
 * [com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentEventHandler] to decide what action needs to be taken
 * in partial payments flow. This class is used to distinguish separate actions that can be taken when submit button
 * is clicked.
 */
@Parcelize
sealed class GiftCardAction : Parcelable {

    /**
     * No action to be taken.
     */
    object Idle : GiftCardAction()

    /**
     * Check balance of the partial payment method.
     */
    object CheckBalance : GiftCardAction()

    /**
     * Submit the payment.
     */
    object SendPayment : GiftCardAction()

    /**
     * Create an order.
     */
    object CreateOrder : GiftCardAction()
}
