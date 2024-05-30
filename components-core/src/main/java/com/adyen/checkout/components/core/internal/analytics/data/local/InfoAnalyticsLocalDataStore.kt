/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.local

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList

internal class InfoAnalyticsLocalDataStore : AnalyticsLocalDataStore<AnalyticsEvent.Info> {

    private val list = LinkedList<AnalyticsEvent.Info>()

    private val mutex = Mutex()

    override suspend fun storeEvent(event: AnalyticsEvent.Info) {
        mutex.withLock {
            list.add(event)
        }
    }

    override suspend fun fetchEvents(size: Int) = mutex.withLock {
        list.takeLast(size)
    }

    override suspend fun removeEvents(events: List<AnalyticsEvent.Info>) {
        mutex.withLock {
            list.removeAll(events.toSet())
        }
    }
}
