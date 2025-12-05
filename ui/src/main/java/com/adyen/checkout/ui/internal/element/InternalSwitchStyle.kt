/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalSwitchStyle(
    val checkedHandleColor: Color,
    val checkedTrackColor: Color,
    val uncheckedHandleColor: Color,
    val uncheckedTrackColor: Color,
)
