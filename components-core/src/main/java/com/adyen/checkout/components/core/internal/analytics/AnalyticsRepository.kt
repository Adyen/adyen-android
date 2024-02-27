/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.internal.data.api.AnalyticsTrackRequestProvider

internal interface AnalyticsRepository {

    suspend fun fetchCheckoutAttemptId(analyticsSetupProvider: AnalyticsSetupProvider): String?

    fun storeEvent(event: AnalyticsEvent)

    fun getEvents(): List<AnalyticsEvent>

    suspend fun sendEvents(events: List<AnalyticsEvent>, checkoutAttemptId: String)
}

internal class DefaultAnalyticsRepository(
    private val localDataStore: AnalyticsLocalDataStore,
    private val remoteDataStore: AnalyticsRemoteDataStore,
    private val analyticsTrackRequestProvider: AnalyticsTrackRequestProvider,
) : AnalyticsRepository {

    override suspend fun fetchCheckoutAttemptId(analyticsSetupProvider: AnalyticsSetupProvider): String? {
        val request = analyticsSetupProvider.provide()
        return remoteDataStore.fetchCheckoutAttemptId(request).checkoutAttemptId
    }

    override fun storeEvent(event: AnalyticsEvent) {
        TODO("Not yet implemented")
    }

    override fun getEvents(): List<AnalyticsEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun sendEvents(
        events: List<AnalyticsEvent>,
        checkoutAttemptId: String,
    ) {
        val request = analyticsTrackRequestProvider(events)
        remoteDataStore.sendEvents(request, checkoutAttemptId)
    }
}
