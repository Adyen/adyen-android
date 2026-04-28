package com.adyen.checkout.giftcard

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class is used in [com.adyen.checkout.giftcard.internal.GiftCardComponentEventHandler] and
 * [com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentEventHandler] to decide what action needs to be taken
 * in partial payments flow. This class is used to distinguish separate actions that can be taken when submit button
 * is clicked.
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
@SuppressLint("ObjectInPublicSealedClass")
@Parcelize
sealed class GiftCardAction : Parcelable {

    /**
     * No action to be taken.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    object Idle : GiftCardAction()

    /**
     * Check balance of the partial payment method.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    object CheckBalance : GiftCardAction()

    /**
     * Submit the payment.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    object SendPayment : GiftCardAction()

    /**
     * Create an order.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    object CreateOrder : GiftCardAction()
}
