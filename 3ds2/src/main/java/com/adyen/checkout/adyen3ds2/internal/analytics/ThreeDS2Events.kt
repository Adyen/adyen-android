/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2024.
 */

package com.adyen.checkout.adyen3ds2.internal.analytics

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.DirectAnalyticsEventCreation

@OptIn(DirectAnalyticsEventCreation::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal object ThreeDS2Events {

    fun threeDS2Fingerprint(
        subType: SubType,
        result: Result? = null,
        message: String? = null,
    ) = AnalyticsEvent.Log(
        component = "threeDS2Fingerprint",
        type = AnalyticsEvent.Log.Type.THREEDS2,
        subType = subType.value,
        result = result?.value,
        message = message,
    )

    fun threeDS2Challenge(
        subType: SubType,
        result: Result? = null,
        message: String? = null,
    ) = AnalyticsEvent.Log(
        component = "threeDS2Challenge",
        type = AnalyticsEvent.Log.Type.THREEDS2,
        subType = subType.value,
        result = result?.value,
        message = message,
    )

    enum class SubType(val value: String) {
        FINGERPRINT_DATA_SENT("fingerprintDataSentMobile"),
        FINGERPRINT_COMPLETED("fingerprintCompleted"),
        CHALLENGE_DATA_SENT("challengeDataSentMobile"),
        CHALLENGE_DISPLAYED("challengeDisplayed"),
        CHALLENGE_COMPLETED("challengeCompleted"),
    }

    enum class Result(val value: String) {
        CANCELLED("cancelled"),
        COMPLETED("completed"),
        TIMEOUT("timeout"),
        ERROR("error"),
        REDIRECT("redirect"),
        THREEDS2("threeDS2"),
    }
}
