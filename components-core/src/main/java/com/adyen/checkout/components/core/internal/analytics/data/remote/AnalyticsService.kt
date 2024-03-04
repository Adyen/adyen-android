/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.remote

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackRequest
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import com.adyen.checkout.core.internal.data.model.EmptyResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    internal suspend fun setupAnalytics(
        request: AnalyticsSetupRequest,
        clientKey: String,
    ): AnalyticsSetupResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v3/analytics",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = AnalyticsSetupRequest.SERIALIZER,
            responseSerializer = AnalyticsSetupResponse.SERIALIZER,
        )
    }

    // TODO: Add tests
    internal suspend fun sendEvents(
        request: AnalyticsTrackRequest,
        checkoutAttemptId: String,
        clientKey: String,
    ) {
        httpClient.post(
            path = "v3/analytics/${checkoutAttemptId}",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = AnalyticsTrackRequest.SERIALIZER,
            responseSerializer = EmptyResponse.SERIALIZER,
        )
    }
}
