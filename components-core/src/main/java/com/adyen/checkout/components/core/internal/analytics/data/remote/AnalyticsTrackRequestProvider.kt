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
            info = infoList.mapToTrackEvent(),
            logs = logList.mapToTrackEvent(),
        )
    }

    private fun List<AnalyticsEvent.Info>.mapToTrackEvent() = map { event ->
        AnalyticsTrackInfo(
            timestamp = event.timestamp,
            component = event.component,
            type = event.type?.value,
            target = event.target,
            isStoredPaymentMethod = event.isStoredPaymentMethod,
            brand = event.brand,
            issuer = event.issuer,
            validationErrorCode = event.validationErrorCode,
            validationErrorMessage = event.validationErrorMessage,
        )
    }

    private fun List<AnalyticsEvent.Log>.mapToTrackEvent() = map { event ->
        AnalyticsTrackLog(
            timestamp = event.timestamp,
            component = event.component,
            type = event.type?.value,
            subType = event.subType,
            target = event.target,
            message = event.message,
        )
    }
}
