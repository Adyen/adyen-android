/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AnalyticsPlatform(val value: String) {
    ANDROID("android"),
    FLUTTER("flutter"),

    @Suppress("unused")
    REACT_NATIVE("react-native"),
}
