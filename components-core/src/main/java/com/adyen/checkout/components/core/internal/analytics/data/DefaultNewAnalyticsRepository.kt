/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.data.local.AnalyticsLocalDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsRemoteDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsSetupProvider
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsTrackRequestProvider

internal class DefaultNewAnalyticsRepository(
    private val localInfoDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Info>,
    private val localLogDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Log>,
    private val remoteDataStore: AnalyticsRemoteDataStore,
    private val analyticsSetupProvider: AnalyticsSetupProvider,
    private val analyticsTrackRequestProvider: AnalyticsTrackRequestProvider,
) : NewAnalyticsRepository {

    override suspend fun fetchCheckoutAttemptId(): String? {
        val request = analyticsSetupProvider.provide()
        return remoteDataStore.fetchCheckoutAttemptId(request).checkoutAttemptId
    }

    override suspend fun storeEvent(event: AnalyticsEvent) {
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

        if (infoEvents.isEmpty() && logEvents.isEmpty()) return

        val request = analyticsTrackRequestProvider(
            infoList = infoEvents,
            logList = logEvents,
        )
        remoteDataStore.sendEvents(request, checkoutAttemptId)
        localInfoDataStore.clear()
        localLogDataStore.clear()
    }
}
