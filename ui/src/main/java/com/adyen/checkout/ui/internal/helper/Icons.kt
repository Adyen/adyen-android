/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2025.
 */

package com.adyen.checkout.ui.internal.helper

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Stable
@Composable
fun getThemedIcon(
    backgroundColor: Color,
    lightDrawableId: Int,
    darkDrawableId: Int,
): Int {
    val isDark = backgroundColor.isDark()
    return remember(backgroundColor) {
        if (isDark) darkDrawableId else lightDrawableId
    }
}
