/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.analytics.data

import com.adyen.checkout.core.internal.analytics.AnalyticsEvent

internal interface AnalyticsRepository {

    suspend fun fetchCheckoutAttemptId(): String?

    suspend fun storeEvent(event: AnalyticsEvent)

    suspend fun sendEvents(checkoutAttemptId: String)
}
