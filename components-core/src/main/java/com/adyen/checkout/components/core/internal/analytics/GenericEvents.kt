/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

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
        type = AnalyticsEvent.Info.Type.RENDERED,
        isStoredPaymentMethod = isStoredPaymentMethod,
        brand = brand,
    )

    fun displayed(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.DISPLAYED,
        target = target,
    )

    fun selected(
        component: String,
        target: String,
        issuer: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.SELECTED,
        target = target,
        issuer = issuer,
    )

    fun input(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.INPUT,
        target = target,
    )

    // TODO: This might move to the Input fields itself and not be bound to any component. But we should find a way to define targets.
    // TODO: We could create an enum for target per module
    fun focus(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.FOCUS,
        target = target,
    )

    fun unFocus(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.UNFOCUS,
        target = target,
    )

    fun download(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.DOWNLOAD,
        target = target,
    )

    fun invalidField(
        component: String,
        target: String,
        validationErrorCode: String?,
        validationErrorMessage: String?,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.VALIDATION_ERROR,
        target = target,
        validationErrorCode = validationErrorCode,
        validationErrorMessage = validationErrorMessage,
    )

    // Log events
    fun submit(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.SUBMIT,
    )

    fun threeDS2(
        component: String,
        message: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.THREEDS2,
        message = message,
    )

    fun action(
        component: String,
        subType: String,
        message: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.ACTION,
        subType = subType,
        message = message,
    )
}
