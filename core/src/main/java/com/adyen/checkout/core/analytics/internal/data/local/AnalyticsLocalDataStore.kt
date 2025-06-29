/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.local

internal interface AnalyticsLocalDataStore<T> {

    suspend fun storeEvent(event: T)

    suspend fun fetchEvents(size: Int): List<T>

    suspend fun removeEvents(events: List<T>)
}
