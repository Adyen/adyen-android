/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data

internal interface AnalyticsLocalDataStore<T> {

    suspend fun storeEvent(event: T)

    suspend fun fetchEvents(size: Int): List<T>

    suspend fun clear()
}
