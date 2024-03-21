/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */

package com.adyen.checkout.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.AnalyticsConfiguration
import com.adyen.checkout.core.AnalyticsLevel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AnalyticsParams(
    val level: AnalyticsParamsLevel,
    val clientKey: String,
) {

    constructor(
        analyticsConfiguration: AnalyticsConfiguration?,
        clientKey: String,
    ) : this(level = getLevel(analyticsConfiguration), clientKey = clientKey)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AnalyticsParamsLevel(val priority: Int) {
    NONE(1),
    ALL(2),
}

private fun getLevel(analyticsConfiguration: AnalyticsConfiguration?): AnalyticsParamsLevel {
    return when (analyticsConfiguration?.level) {
        null -> AnalyticsParamsLevel.ALL // default is ALL
        AnalyticsLevel.ALL -> AnalyticsParamsLevel.ALL
        AnalyticsLevel.NONE -> AnalyticsParamsLevel.NONE
    }
}
