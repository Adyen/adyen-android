/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2023.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AnalyticsPlatform(val value: String) {
    ANDROID("android"),
    FLUTTER("flutter"),
    REACT_NATIVE("react-native"),
}
