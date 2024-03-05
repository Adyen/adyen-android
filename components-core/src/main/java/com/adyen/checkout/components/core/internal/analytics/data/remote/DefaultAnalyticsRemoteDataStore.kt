/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.remote

import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackRequest

internal class DefaultAnalyticsRemoteDataStore(
    private val analyticsService: AnalyticsService,
    private val clientKey: String,
    override val infoSize: Int,
    override val logSize: Int,
) : AnalyticsRemoteDataStore {

    override suspend fun fetchCheckoutAttemptId(request: AnalyticsSetupRequest): AnalyticsSetupResponse {
        return analyticsService.setupAnalytics(request, clientKey)
    }

    override suspend fun sendEvents(
        request: AnalyticsTrackRequest,
        checkoutAttemptId: String,
    ) {
        analyticsService.sendEvents(request, checkoutAttemptId, clientKey)
    }
}
