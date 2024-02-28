/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSetupProvider
import com.adyen.checkout.components.core.internal.data.api.AnalyticsTrackRequestProvider

internal class DefaultAnalyticsRepository(
    private val localInfoDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Info>,
    private val localLogDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Log>,
    private val remoteDataStore: AnalyticsRemoteDataStore,
    private val analyticsTrackRequestProvider: AnalyticsTrackRequestProvider,
) : AnalyticsRepository {

    override suspend fun fetchCheckoutAttemptId(analyticsSetupProvider: AnalyticsSetupProvider): String? {
        val request = analyticsSetupProvider.provide()
        return remoteDataStore.fetchCheckoutAttemptId(request).checkoutAttemptId
    }

    override fun storeEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.Info -> localInfoDataStore.storeEvent(event)
            is AnalyticsEvent.Log -> localLogDataStore.storeEvent(event)
        }
    }

    override suspend fun sendEvents(
        checkoutAttemptId: String,
    ) {
        val infoEvents = localInfoDataStore.fetchEvents(remoteDataStore.infoSize)
        val logEvents = localLogDataStore.fetchEvents(remoteDataStore.logSize)
        val request = analyticsTrackRequestProvider(
            infoList = infoEvents,
            logList = logEvents,
        )
        remoteDataStore.sendEvents(request, checkoutAttemptId)
    }
}
