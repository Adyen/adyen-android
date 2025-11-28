/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2024.
 */

package com.adyen.checkout.adyen3ds2.internal.analytics

import com.adyen.checkout.core.analytics.internal.AnalyticsEvent
import com.adyen.checkout.core.analytics.internal.DirectAnalyticsEventCreation
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents

@OptIn(DirectAnalyticsEventCreation::class)
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

    fun threeDS2FingerprintError(
        event: ErrorEvent,
        message: String? = null
    ) = GenericEvents.error(
        component = "threeDS2Fingerprint",
        event = event,
        message = message,
    )

    fun threeDS2ChallengeError(
        event: ErrorEvent,
        message: String? = null
    ) = GenericEvents.error(
        component = "threeDS2Challenge",
        event = event,
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
