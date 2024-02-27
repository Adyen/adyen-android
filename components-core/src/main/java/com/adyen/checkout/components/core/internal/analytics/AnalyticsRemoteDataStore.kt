/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse

internal interface AnalyticsRemoteDataStore {

    suspend fun fetchCheckoutAttemptId(request: AnalyticsSetupRequest): AnalyticsSetupResponse

    suspend fun sendEvents()
}

internal class DefaultAnalyticsRemoteDataStore(
    private val analyticsService: AnalyticsService,
    private val clientKey: String,
) : AnalyticsRemoteDataStore {

    override suspend fun fetchCheckoutAttemptId(request: AnalyticsSetupRequest): AnalyticsSetupResponse {
        return analyticsService.setupAnalytics(request, clientKey)
    }

    override suspend fun sendEvents() {
        TODO("Not yet implemented")
    }
}
