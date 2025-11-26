/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/4/2025.
 */

package com.adyen.checkout.ui.internal.button

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class InternalButtonStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val disabledBackgroundColor: Color,
    val disabledTextColor: Color,
    val cornerRadius: Int,
)
