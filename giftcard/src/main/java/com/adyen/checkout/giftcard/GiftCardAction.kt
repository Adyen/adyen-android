package com.adyen.checkout.giftcard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO Add more information here what the actions means
@Parcelize
sealed class GiftCardAction : Parcelable {
    object Idle : GiftCardAction()
    object CheckBalance : GiftCardAction()
    object SendPayment : GiftCardAction()
    object CreateOrder : GiftCardAction()
}
