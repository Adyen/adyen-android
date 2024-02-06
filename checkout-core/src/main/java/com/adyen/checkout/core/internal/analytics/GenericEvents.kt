/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2024.
 */

package com.adyen.checkout.core.internal.analytics

import androidx.annotation.RestrictTo

@OptIn(AnalyticsEventApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object GenericEvents {

    // Info events
    fun rendered(
        component: String,
        // Check if this should be null or false by default
        isStoredPaymentMethod: Boolean,
        brand: String? = null,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.RENDERED,
        isStoredPaymentMethod = isStoredPaymentMethod,
        brand = brand,
    )

    fun displayed(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.DISPLAYED,
        target = target,
    )

    fun selected(
        component: String,
        target: String,
        issuer: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.SELECTED,
        target = target,
        issuer = issuer,
    )

    fun input(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.INPUT,
        target = target,
    )

    // TODO: This might move to the Input fields itself and not be bound to any component. But we should find a way to define targets.
    // TODO: We could create an enum for target per module
    fun focus(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.FOCUS,
        target = target,
    )

    fun unFocus(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.UNFOCUS,
        target = target,
    )

    fun download(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.DOWNLOAD,
        target = target,
    )

    fun invalidField(
        component: String,
        target: String,
        validationErrorCode: String?,
        validationErrorMessage: String?,
    ) = AnalyticsEvent.Info(
        component = component,
        type = InfoEventType.VALIDATION_ERROR,
        target = target,
        validationErrorCode = validationErrorCode,
        validationErrorMessage = validationErrorMessage,
    )

    // Log events
    fun submit(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = LogEventType.SUBMIT,
    )

    fun threeDS2(
        component: String,
        message: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = LogEventType.THREEDS2,
        message = message,
    )

    fun action(
        component: String,
        subType: String,
        message: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = LogEventType.ACTION,
        subType = subType,
        message = message,
    )
}
