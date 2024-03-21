/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */

package com.adyen.checkout.core.internal.analytics

import androidx.annotation.RestrictTo

@Suppress("unused")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AnalyticsPlatform(val value: String) {
    ANDROID("android"),
    FLUTTER("flutter"),
    REACT_NATIVE("react-native"),
}
