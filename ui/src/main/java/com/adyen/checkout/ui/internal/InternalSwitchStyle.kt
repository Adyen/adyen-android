/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class InternalSwitchStyle(
    val checkedHandleColor: Color,
    val checkedTrackColor: Color,
    val uncheckedHandleColor: Color,
    val uncheckedTrackColor: Color,
)
