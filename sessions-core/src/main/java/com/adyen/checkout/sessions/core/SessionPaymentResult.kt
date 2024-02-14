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
 * The result of a payment using the sessions flow.
 *
 * @param sessionId A unique identifier of the session.
 * @param sessionResult You can forward this alongside [sessionId] to your server to fetch the result of the payment.
 * @param sessionData The payment session data.
 * @param resultCode The result code of the payment. For more information, see
 * [Result codes](https://docs.adyen.com/online-payments/build-your-integration/payment-result-codes/).
 * @param order An order, only applicable in case of an ongoing partial payment flow.
 */
@Parcelize
data class SessionPaymentResult(
    val sessionId: String?,
    val sessionResult: String?,
    val sessionData: String?,
    val resultCode: String?,
    val order: OrderResponse?,
) : Parcelable
