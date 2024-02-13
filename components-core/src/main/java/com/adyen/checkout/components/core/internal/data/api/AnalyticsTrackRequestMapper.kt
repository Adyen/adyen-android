/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/2/2024.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackInfo
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackLog
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackRequest

internal class AnalyticsTrackRequestMapper {

    operator fun invoke(
        channel: String,
        events: List<AnalyticsEvent>
    ): AnalyticsTrackRequest {
        val infoList = mutableListOf<AnalyticsTrackInfo>()
        val logList = mutableListOf<AnalyticsTrackLog>()
        events.forEach { analyticsEvent ->
            when (analyticsEvent) {
                is AnalyticsEvent.Info -> {
                    val info = mapInfo(analyticsEvent)
                    infoList.add(info)
                }

                is AnalyticsEvent.Log -> {
                    val log = mapLog(analyticsEvent)
                    logList.add(log)
                }
            }
        }

        return AnalyticsTrackRequest(
            channel = channel,
            info = infoList,
            logs = logList,
        )
    }

    private fun mapInfo(analyticsEvent: AnalyticsEvent.Info) = AnalyticsTrackInfo(
        timestamp = analyticsEvent.timestamp,
        component = analyticsEvent.component,
        type = analyticsEvent.type?.value,
        target = analyticsEvent.target,
        isStoredPaymentMethod = analyticsEvent.isStoredPaymentMethod,
        brand = analyticsEvent.brand,
        issuer = analyticsEvent.issuer,
        validationErrorCode = analyticsEvent.validationErrorCode,
        validationErrorMessage = analyticsEvent.validationErrorMessage,
    )

    private fun mapLog(analyticsEvent: AnalyticsEvent.Log) = AnalyticsTrackLog(
        timestamp = analyticsEvent.timestamp,
        component = analyticsEvent.component,
        type = analyticsEvent.type?.value,
        subType = analyticsEvent.subType,
        target = analyticsEvent.target,
        message = analyticsEvent.message,
    )
}
