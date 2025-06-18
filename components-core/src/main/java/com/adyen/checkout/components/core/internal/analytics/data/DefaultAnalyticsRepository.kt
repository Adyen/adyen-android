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
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog

internal class DefaultAnalyticsRepository(
    private val localInfoDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Info>,
    private val localLogDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Log>,
    private val localErrorDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Error>,
    private val remoteDataStore: AnalyticsRemoteDataStore,
    private val analyticsSetupProvider: AnalyticsSetupProvider,
    private val analyticsTrackRequestProvider: AnalyticsTrackRequestProvider,
) : AnalyticsRepository {

    override suspend fun fetchCheckoutAttemptId(): String? {
        val request = analyticsSetupProvider.provide()
        return remoteDataStore.fetchCheckoutAttemptId(request).checkoutAttemptId
    }

    override suspend fun storeEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.Info -> localInfoDataStore.storeEvent(event)
            is AnalyticsEvent.Log -> localLogDataStore.storeEvent(event)
            is AnalyticsEvent.Error -> localErrorDataStore.storeEvent(event)
        }
    }

    override suspend fun sendEvents(
        checkoutAttemptId: String,
    ) {
        val infoEvents = localInfoDataStore.fetchEvents(remoteDataStore.infoSize)
        val logEvents = localLogDataStore.fetchEvents(remoteDataStore.logSize)
        val errorEvents = localErrorDataStore.fetchEvents(remoteDataStore.errorSize)

        if (!hasEventsToTrack(infoEvents, logEvents, errorEvents)) return

        val request = analyticsTrackRequestProvider(
            infoList = infoEvents,
            logList = logEvents,
            errorList = errorEvents,
        )
        remoteDataStore.sendEvents(request, checkoutAttemptId)

        localInfoDataStore.removeEvents(infoEvents)
        localLogDataStore.removeEvents(logEvents)
        localErrorDataStore.removeEvents(errorEvents)

        adyenLog(AdyenLogLevel.DEBUG) { "Analytics events successfully sent" }
    }

    private fun hasEventsToTrack(vararg eventLists: List<AnalyticsEvent>): Boolean {
        for (events in eventLists) {
            if (events.isNotEmpty()) {
                return true
            }
        }
        return false
    }
}
