/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable

/**
 * Identifies a pending focus request without exposing its form element ID to the UI module.
 */
@Immutable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmInline
value class FocusRequestToken(private val request: Any)
