/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.old.GooglePayComponentState
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback

internal data class SessionsGooglePayComponentData(
    val checkoutSession: CheckoutSession,
    val checkoutConfiguration: CheckoutConfiguration,
    val paymentMethod: PaymentMethod,
    val callback: SessionComponentCallback<GooglePayComponentState>
)
