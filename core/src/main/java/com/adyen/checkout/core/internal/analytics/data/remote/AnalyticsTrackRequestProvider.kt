/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.analytics.data.remote

import com.adyen.checkout.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.core.internal.analytics.AnalyticsPlatformParams
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackError
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackInfo
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackLog
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackRequest

internal class AnalyticsTrackRequestProvider {

    operator fun invoke(
        infoList: List<AnalyticsEvent.Info>,
        logList: List<AnalyticsEvent.Log>,
        errorList: List<AnalyticsEvent.Error>,
    ): AnalyticsTrackRequest {
        return AnalyticsTrackRequest(
            channel = AnalyticsPlatformParams.channel,
            platform = AnalyticsPlatformParams.platform,
            info = infoList.map { event -> event.mapToTrackEvent() },
            logs = logList.map { event -> event.mapToTrackEvent() },
            errors = errorList.map { event -> event.mapToErrorEvent() },
        )
    }

    private fun AnalyticsEvent.Info.mapToTrackEvent() = AnalyticsTrackInfo(
        id = id,
        timestamp = timestamp,
        component = component,
        type = type?.value,
        target = target,
        isStoredPaymentMethod = isStoredPaymentMethod,
        brand = brand,
        issuer = issuer,
        validationErrorCode = validationErrorCode,
        validationErrorMessage = validationErrorMessage,
        configData = configData,
    )

    private fun AnalyticsEvent.Log.mapToTrackEvent() = AnalyticsTrackLog(
        id = id,
        timestamp = timestamp,
        component = component,
        type = type?.value,
        subType = subType,
        target = target,
        message = message,
        result = result,
    )

    private fun AnalyticsEvent.Error.mapToErrorEvent() = AnalyticsTrackError(
        id = id,
        timestamp = timestamp,
        component = component,
        errorType = errorType?.value,
        code = code,
        target = target,
        message = message,
    )
}
