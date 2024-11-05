/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2024.
 */

package com.adyen.checkout.dropin.internal.analytics

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.DirectAnalyticsEventCreation
import com.adyen.checkout.components.core.internal.analytics.GenericEvents

private const val ANALYTICS_COMPONENT = "dropin"

@OptIn(DirectAnalyticsEventCreation::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal object DropInEvents {

    fun rendered(
        configData: Map<String, String>? = null,
    ) = GenericEvents.rendered(
        component = ANALYTICS_COMPONENT,
        configData = configData,
    )

    fun closed(
        message: String? = null,
    ) = AnalyticsEvent.Log(
        component = ANALYTICS_COMPONENT,
        type = AnalyticsEvent.Log.Type.CLOSED,
        message = message,
    )
}
