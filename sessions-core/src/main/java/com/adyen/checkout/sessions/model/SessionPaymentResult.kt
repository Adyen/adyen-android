/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.sessions.model

import com.adyen.checkout.components.model.payments.response.OrderResponse

// TODO SESSIONS: docs
data class SessionPaymentResult(
    val sessionResult: String?,
    val sessionData: String,
    val resultCode: String?,
    // TODO SESSIONS: check if we can return this to the merchant
    val order: OrderResponse?,
)
