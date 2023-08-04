/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsService(
    private val httpClient: HttpClient,
) {

    internal suspend fun setupAnalytics(
        request: AnalyticsSetupRequest,
        clientKey: String,
    ): AnalyticsSetupResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v2/analytics",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = AnalyticsSetupRequest.SERIALIZER,
            responseSerializer = AnalyticsSetupResponse.SERIALIZER
        )
    }
}
