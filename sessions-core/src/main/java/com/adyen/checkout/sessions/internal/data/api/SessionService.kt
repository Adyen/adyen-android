/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.sessions.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import com.adyen.checkout.sessions.SessionSetupResponse
import com.adyen.checkout.sessions.internal.data.model.SessionBalanceRequest
import com.adyen.checkout.sessions.internal.data.model.SessionBalanceResponse
import com.adyen.checkout.sessions.internal.data.model.SessionCancelOrderRequest
import com.adyen.checkout.sessions.internal.data.model.SessionCancelOrderResponse
import com.adyen.checkout.sessions.internal.data.model.SessionDetailsRequest
import com.adyen.checkout.sessions.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.sessions.internal.data.model.SessionOrderRequest
import com.adyen.checkout.sessions.internal.data.model.SessionOrderResponse
import com.adyen.checkout.sessions.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.sessions.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.sessions.internal.data.model.SessionSetupRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionService(
    private val httpClient: HttpClient,
) {

    suspend fun setupSession(
        request: SessionSetupRequest,
        sessionId: String,
        clientKey: String,
    ): SessionSetupResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/setup",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionSetupRequest.SERIALIZER,
            responseSerializer = SessionSetupResponse.SERIALIZER,
        )
    }

    suspend fun submitPayment(
        request: SessionPaymentsRequest,
        sessionId: String,
        clientKey: String,
    ): SessionPaymentsResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/payments",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionPaymentsRequest.SERIALIZER,
            responseSerializer = SessionPaymentsResponse.SERIALIZER,
        )
    }

    suspend fun submitDetails(
        request: SessionDetailsRequest,
        sessionId: String,
        clientKey: String,
    ): SessionDetailsResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/paymentDetails",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionDetailsRequest.SERIALIZER,
            responseSerializer = SessionDetailsResponse.SERIALIZER,
        )
    }

    suspend fun checkBalance(
        request: SessionBalanceRequest,
        sessionId: String,
        clientKey: String,
    ): SessionBalanceResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/paymentMethodBalance",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionBalanceRequest.SERIALIZER,
            responseSerializer = SessionBalanceResponse.SERIALIZER,
        )
    }

    suspend fun createOrder(
        request: SessionOrderRequest,
        sessionId: String,
        clientKey: String,
    ): SessionOrderResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/orders",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionOrderRequest.SERIALIZER,
            responseSerializer = SessionOrderResponse.SERIALIZER,
        )
    }

    suspend fun cancelOrder(
        request: SessionCancelOrderRequest,
        sessionId: String,
        clientKey: String,
    ): SessionCancelOrderResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/sessions/$sessionId/orders/cancel",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionCancelOrderRequest.SERIALIZER,
            responseSerializer = SessionCancelOrderResponse.SERIALIZER,
        )
    }
}
