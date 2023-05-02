/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.sessions.core.CheckoutSession

internal data class SessionsGiftCardComponentData(
    val checkoutSession: CheckoutSession,
    val paymentMethod: PaymentMethod,
    val callback: SessionsGiftCardComponentCallback,
)
