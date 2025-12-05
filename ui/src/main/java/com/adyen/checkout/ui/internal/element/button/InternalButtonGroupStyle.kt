/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/7/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalButtonGroupStyle(
    val checkedContainerColor: Color,
    val checkedTextColor: Color,
    val uncheckedContainerColor: Color,
    val uncheckedTextColor: Color,
    val disabledContentColor: Color,
    val disabledContainerColor: Color,
)
