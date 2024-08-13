package com.adyen.checkout.giftcard

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@SuppressLint("ObjectInPublicSealedClass")
@Parcelize
sealed class GiftCardAction : Parcelable {
    object Idle : GiftCardAction()
    object CheckBalance : GiftCardAction()
    object SendPayment : GiftCardAction()
    object CreateOrder : GiftCardAction()
}
