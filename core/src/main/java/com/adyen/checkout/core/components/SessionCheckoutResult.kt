/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.common.CheckoutResultCode

/**
 * The result of a payment using the sessions flow.
 *
 * @property resultCode The result code of the payment.
 * @property sessionId A unique identifier of the session.
 * @property sessionData The payment session data. You can forward this alongside [sessionId] to your server to fetch
 * the result of the payment.
 */
data class SessionCheckoutResult(
    val resultCode: CheckoutResultCode,
    val sessionId: String,
    val sessionData: String,
)
