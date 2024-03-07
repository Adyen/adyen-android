/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo
import java.util.Date

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface AnalyticsEvent {

    val timestamp: Long
    val shouldForceSend: Boolean
    val component: String

    data class Info @DirectAnalyticsEventCreation constructor(
        override val timestamp: Long = Date().time,
        override val shouldForceSend: Boolean = false,
        override val component: String,
        val type: Type? = null,
        val target: String? = null,
        val isStoredPaymentMethod: Boolean? = null,
        val brand: String? = null,
        val issuer: String? = null,
        val validationErrorCode: String? = null,
        val validationErrorMessage: String? = null,
    ) : AnalyticsEvent {
        enum class Type(val value: String) {
            DISPLAYED("displayed"),
            DOWNLOAD("download"),
            FOCUS("focus"),
            INPUT("input"),
            RENDERED("rendered"),
            SELECTED("selected"),
            UNFOCUS("unfocus"),
            VALIDATION_ERROR("validationError"),
        }
    }

    data class Log @DirectAnalyticsEventCreation constructor(
        override val timestamp: Long = Date().time,
        override val shouldForceSend: Boolean = true,
        override val component: String,
        val type: Type? = null,
        val subType: String? = null,
        val target: String? = null,
        val message: String? = null,
    ) : AnalyticsEvent {
        enum class Type(val value: String) {
            ACTION("action"),
            SUBMIT("submit"),
            THREEDS2("ThreeDS2"),
        }
    }
}

// TODO: Does it inherit the restrictTo from the Type?
@Suppress("ForbiddenComment")
typealias InfoEventType = AnalyticsEvent.Info.Type

typealias LogEventType = AnalyticsEvent.Log.Type
