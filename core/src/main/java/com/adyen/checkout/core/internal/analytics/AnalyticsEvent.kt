/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.analytics

import androidx.annotation.RestrictTo
import java.util.Date
import java.util.UUID

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface AnalyticsEvent {

    val id: String
    val timestamp: Long
    val shouldForceSend: Boolean
    val component: String

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Info @DirectAnalyticsEventCreation constructor(
        override val id: String = UUID.randomUUID().toString(),
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
        val configData: Map<String, String>? = null,
    ) : AnalyticsEvent {

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Log @DirectAnalyticsEventCreation constructor(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = Date().time,
        override val shouldForceSend: Boolean = true,
        override val component: String,
        val type: Type? = null,
        val subType: String? = null,
        val result: String? = null,
        val target: String? = null,
        val message: String? = null,
    ) : AnalyticsEvent {

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        enum class Type(val value: String) {
            ACTION("action"),
            CARD_SCANNER("CardScanner"),
            SUBMIT("submit"),
            CLOSED("closed"),
            THREEDS2("ThreeDS2"),
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Error @DirectAnalyticsEventCreation constructor(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = Date().time,
        override val shouldForceSend: Boolean = true,
        override val component: String,
        val errorType: Type? = null,
        val code: String? = null,
        val target: String? = null,
        val message: String? = null,
    ) : AnalyticsEvent {

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        enum class Type(val value: String) {
            REDIRECT("Redirect"),
            INTERNAL("Internal"),
            THIRD_PARTY("ThirdParty"),
            API_ERROR("ApiError"),
            THREEDS2("ThreeDS2"),
        }
    }
}
