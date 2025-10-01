/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.api

import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.post
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SessionService(
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
}
