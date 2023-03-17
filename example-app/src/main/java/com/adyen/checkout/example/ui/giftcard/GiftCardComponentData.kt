package com.adyen.checkout.example.ui.giftcard

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.giftcard.GiftCardComponentCallback

class GiftCardComponentData(
    val paymentMethod: PaymentMethod,
    val callback: GiftCardComponentCallback,
)
