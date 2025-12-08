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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Color.isDark() = luminance() < LUMINANCE_THRESHOLD
private const val LUMINANCE_THRESHOLD = 0.5f
