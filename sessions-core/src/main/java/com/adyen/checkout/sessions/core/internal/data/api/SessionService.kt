/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.sessions.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import com.adyen.checkout.sessions.core.SessionSetupResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionBalanceRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionBalanceResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionCancelOrderRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionCancelOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetailsRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDisableTokenRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionDisableTokenResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionOrderRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionSetupRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    suspend fun setupSession(
        request: SessionSetupRequest,
        sessionId: String,
        clientKey: String,
    ): SessionSetupResponse = withContext(coroutineDispatcher) {
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
    ): SessionPaymentsResponse = withContext(coroutineDispatcher) {
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
    ): SessionDetailsResponse = withContext(coroutineDispatcher) {
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
    ): SessionBalanceResponse = withContext(coroutineDispatcher) {
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
    ): SessionOrderResponse = withContext(coroutineDispatcher) {
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
    ): SessionCancelOrderResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v1/sessions/$sessionId/orders/cancel",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionCancelOrderRequest.SERIALIZER,
            responseSerializer = SessionCancelOrderResponse.SERIALIZER,
        )
    }

    suspend fun disableToken(
        request: SessionDisableTokenRequest,
        sessionId: String,
        clientKey: String,
    ): SessionDisableTokenResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v1/sessions/$sessionId/disableToken",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = SessionDisableTokenRequest.SERIALIZER,
            responseSerializer = SessionDisableTokenResponse.SERIALIZER,
        )
    }
}
