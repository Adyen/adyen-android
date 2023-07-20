/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/7/2023.
 */

package com.adyen.checkout.components.core.internal.ui.model

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import kotlinx.parcelize.Parcelize

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class AnalyticsParams(
    val level: AnalyticsParamsLevel,
) : Parcelable {

    constructor(analyticsConfiguration: AnalyticsConfiguration?) :
        this(level = getLevel(analyticsConfiguration))
}

enum class AnalyticsParamsLevel {
    ALL,
    NONE,
}

private fun getLevel(analyticsConfiguration: AnalyticsConfiguration?): AnalyticsParamsLevel {
    return when (analyticsConfiguration?.level) {
        null -> AnalyticsParamsLevel.ALL // default is ALL
        AnalyticsLevel.ALL -> AnalyticsParamsLevel.ALL
        AnalyticsLevel.NONE -> AnalyticsParamsLevel.NONE
    }
}
