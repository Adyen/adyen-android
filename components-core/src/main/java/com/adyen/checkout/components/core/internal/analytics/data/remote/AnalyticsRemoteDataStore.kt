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

internal interface AnalyticsRemoteDataStore {

    val infoSize: Int
    val logSize: Int
    val errorSize: Int

    suspend fun fetchCheckoutAttemptId(request: AnalyticsSetupRequest): AnalyticsSetupResponse

    suspend fun sendEvents(request: AnalyticsTrackRequest, checkoutAttemptId: String)
}
