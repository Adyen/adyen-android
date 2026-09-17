/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/9/2026.
 */

package com.adyen.checkout.core.action.data

/**
 * Information about the [Action] that is about to be handled.
 *
 * @param type The type of the action, as returned by the `/payments` endpoint of the Checkout API. For example
 * `redirect`, `nativeRedirect`, `threeDS2`, `sdk`, `qrCode`, `await` or `voucher`.
 */
data class ActionData(
    val type: String,
)
