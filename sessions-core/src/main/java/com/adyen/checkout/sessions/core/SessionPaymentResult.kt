/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.sessions.core

import android.os.Parcelable
import com.adyen.checkout.components.core.OrderResponse
import kotlinx.parcelize.Parcelize

/**
 * The final result of a payment using the sessions flow.
 * You can use the [sessionId] and [sessionResult] to get the result of the payment session on your server.
 */
@Parcelize
data class SessionPaymentResult(
    val sessionId: String?,
    val sessionResult: String?,
    val sessionData: String?,
    val resultCode: String?,
    val order: OrderResponse?,
) : Parcelable
