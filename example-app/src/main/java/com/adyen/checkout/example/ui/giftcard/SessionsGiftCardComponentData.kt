package com.adyen.checkout.example.ui.giftcard

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.sessions.core.CheckoutSession

data class SessionsGiftCardComponentData(
    val checkoutSession: CheckoutSession,
    val paymentMethod: PaymentMethod,
    val callback: SessionsGiftCardComponentCallback,
)
