/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import java.util.LinkedList

internal class InfoAnalyticsLocalDataStore : AnalyticsLocalDataStore<AnalyticsEvent.Info> {

    private val list = LinkedList<AnalyticsEvent.Info>()

    override fun storeEvent(event: AnalyticsEvent.Info) {
        list.add(event)
    }

    override fun fetchEvents(size: Int) = list.takeLast(size)

    override fun clear() {
        list.clear()
    }
}
