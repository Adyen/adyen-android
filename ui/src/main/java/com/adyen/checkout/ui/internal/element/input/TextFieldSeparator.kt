/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo

/**
 * Indexes should not include existing separators but only the raw string.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class TextFieldSeparator(
    val character: Char,
    val indexInRawString: Int,
)
