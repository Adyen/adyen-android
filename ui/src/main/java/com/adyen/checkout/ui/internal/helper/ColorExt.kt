/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2025.
 */

package com.adyen.checkout.ui.internal.helper

import androidx.annotation.RestrictTo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Color.isDark() = luminance() < LUMINANCE_THRESHOLD

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Color.toHex(): String = String.format(Locale.ROOT, "#%06X", RGB_MASK and toArgb())

private const val LUMINANCE_THRESHOLD = 0.5f
private const val RGB_MASK = 0xFFFFFF
