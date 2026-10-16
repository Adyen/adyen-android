/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.api

import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.post
import com.adyen.checkout.dropin.internal.data.model.DisableTokenRequest
import com.adyen.checkout.dropin.internal.data.model.DisableTokenResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Calls the `/sessions` endpoint that removes a stored payment method. Only Drop-in makes this call, the payment calls
 * of a session are made by `core`.
 */
internal class DisableTokenService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    suspend fun disableToken(
        request: DisableTokenRequest,
        sessionId: String,
        clientKey: String,
    ): DisableTokenResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v1/sessions/$sessionId/disableToken",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = DisableTokenRequest.SERIALIZER,
            responseSerializer = DisableTokenResponse.SERIALIZER,
        )
    }
}
