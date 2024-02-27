/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

internal interface AnalyticsRepository {

    suspend fun fetchCheckoutAttemptId(analyticsProvider: AnalyticsProvider): String?

    fun storeEvent(event: AnalyticsEvent)

    fun getEvents(): List<AnalyticsEvent>

    fun sendEvents()
}

internal class DefaultAnalyticsRepository(
    private val localDataStore: AnalyticsLocalDataStore,
    private val remoteDataStore: AnalyticsRemoteDataStore,
) : AnalyticsRepository {

    override suspend fun fetchCheckoutAttemptId(analyticsProvider: AnalyticsProvider): String? {
        val request = analyticsProvider.provide()
        return remoteDataStore.fetchCheckoutAttemptId(request).checkoutAttemptId
    }

    override fun storeEvent(event: AnalyticsEvent) {
        TODO("Not yet implemented")
    }

    override fun getEvents(): List<AnalyticsEvent> {
        TODO("Not yet implemented")
    }

    override fun sendEvents() {
        TODO("Not yet implemented")
    }
}
