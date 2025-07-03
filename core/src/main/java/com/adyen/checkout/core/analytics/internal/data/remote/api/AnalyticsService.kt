/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsSetupRequest
import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsSetupResponse
import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsTrackRequest
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.post
import com.adyen.checkout.core.common.internal.model.EmptyResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
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

    internal suspend fun sendEvents(
        request: AnalyticsTrackRequest,
        checkoutAttemptId: String,
        clientKey: String,
    ): EmptyResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v3/analytics/$checkoutAttemptId",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = AnalyticsTrackRequest.SERIALIZER,
            responseSerializer = EmptyResponse.SERIALIZER,
        )
    }
}
