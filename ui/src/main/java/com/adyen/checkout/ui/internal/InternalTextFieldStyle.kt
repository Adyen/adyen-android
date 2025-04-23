/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.theme.AdyenTextFieldStyle

@Immutable
internal data class InternalTextFieldStyle(
    val type: AdyenTextFieldStyle.Type,
    val backgroundColor: Color,
    val textColor: Color,
    val activeColor: Color,
    val errorColor: Color,
    val cornerRadius: Int,
    val borderColor: Color,
    val borderWidth: Int,
)
