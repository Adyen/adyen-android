/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo

@Suppress("TooManyFunctions")
@OptIn(DirectAnalyticsEventCreation::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object GenericEvents {

    // Info events
    fun rendered(
        component: String,
        isStoredPaymentMethod: Boolean? = null,
        brand: String? = null,
        configData: Map<String, String>? = null,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.RENDERED,
        isStoredPaymentMethod = isStoredPaymentMethod,
        brand = brand,
        configData = configData,
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

    // This might move to the Input fields itself and not be bound to any component. But we should find a way to
    // define targets. We could create an enum for target per module
    @Suppress("Unused")
    fun focus(
        component: String,
        target: String,
    ) = AnalyticsEvent.Info(
        component = component,
        type = AnalyticsEvent.Info.Type.FOCUS,
        target = target,
    )

    // This might move to the Input fields itself and not be bound to any component. But we should find a way to
    // define targets. We could create an enum for target per module
    @Suppress("Unused")
    fun unfocus(
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

    @Suppress("Unused")
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

    fun action(
        component: String,
        subType: String,
        message: String? = null
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.ACTION,
        subType = subType,
        message = message,
    )

    // Error events
    fun error(
        component: String,
        event: ErrorEvent,
        target: String? = null,
    ) = AnalyticsEvent.Error(
        component = component,
        errorType = event.errorType,
        code = event.errorCode,
        target = target,
    )
}
