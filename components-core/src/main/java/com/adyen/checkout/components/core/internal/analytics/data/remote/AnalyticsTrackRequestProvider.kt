/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.remote

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.AnalyticsPlatformParams
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackInfo
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackLog
import com.adyen.checkout.components.core.internal.data.model.AnalyticsTrackRequest

internal class AnalyticsTrackRequestProvider {

    operator fun invoke(
        infoList: List<AnalyticsEvent.Info>,
        logList: List<AnalyticsEvent.Log>,
    ): AnalyticsTrackRequest {
        return AnalyticsTrackRequest(
            channel = AnalyticsPlatformParams.channel,
            platform = AnalyticsPlatformParams.platform,
            info = infoList.map { event -> event.mapToTrackEvent() },
            logs = logList.map { event -> event.mapToTrackEvent() },
        )
    }

    private fun AnalyticsEvent.Info.mapToTrackEvent() = AnalyticsTrackInfo(
        timestamp = timestamp,
        component = component,
        type = type?.value,
        target = target,
        isStoredPaymentMethod = isStoredPaymentMethod,
        brand = brand,
        issuer = issuer,
        validationErrorCode = validationErrorCode,
        validationErrorMessage = validationErrorMessage,
    )

    private fun AnalyticsEvent.Log.mapToTrackEvent() = AnalyticsTrackLog(
        timestamp = timestamp,
        component = component,
        type = type?.value,
        subType = subType,
        target = target,
        message = message,
    )
}
