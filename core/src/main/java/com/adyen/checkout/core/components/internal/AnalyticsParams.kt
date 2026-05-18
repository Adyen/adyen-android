/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AnalyticsParams(
    val level: AnalyticsParamsLevel,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AnalyticsParamsLevel(val priority: Int) {
    INITIAL(1),
    ALL(2),
}
