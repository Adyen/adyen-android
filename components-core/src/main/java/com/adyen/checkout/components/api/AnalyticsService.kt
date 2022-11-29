/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/11/2022.
 */

package com.adyen.checkout.components.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.api.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsService(
    private val httpClient: HttpClient,
) {

    suspend fun sendEvent(
        queryParameters: Map<String, String>,
    ) {
        withContext(Dispatchers.IO) {
            httpClient.get(
                "images/analytics.png",
                queryParameters,
            )
        }
    }
}
