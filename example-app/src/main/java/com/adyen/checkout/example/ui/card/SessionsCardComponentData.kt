/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/1/2023.
 */

package com.adyen.checkout.example.ui.card

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback

internal data class SessionsCardComponentData(
    val checkoutSession: CheckoutSession,
    val paymentMethod: PaymentMethod,
    val callback: SessionComponentCallback
)
