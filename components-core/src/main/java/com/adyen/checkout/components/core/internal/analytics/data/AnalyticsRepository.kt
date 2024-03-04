/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsSetupProvider

internal interface AnalyticsRepository {

    suspend fun fetchCheckoutAttemptId(analyticsSetupProvider: AnalyticsSetupProvider): String?

    suspend fun storeEvent(event: AnalyticsEvent)

    suspend fun sendEvents(checkoutAttemptId: String)
}
