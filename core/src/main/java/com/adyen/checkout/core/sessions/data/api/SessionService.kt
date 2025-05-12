/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import com.adyen.checkout.core.sessions.data.SessionPaymentsRequest
import com.adyen.checkout.core.sessions.data.SessionPaymentsResponse
import com.adyen.checkout.core.sessions.data.SessionSetupRequest
import com.adyen.checkout.core.sessions.data.SessionSetupResponse
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
}
