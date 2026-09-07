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
 * A pending request to move focus to a field. Opaque, because the state layer that describes the request lives in a
 * module this one cannot see, and a field only needs to tell one request from the next.
 *
 * Only wrap an immutable value that compares by equality. Two fields depend on it: the [Immutable] promise below is
 * what stops every view state holding a token from being treated as unstable, and a token that never changes value
 * stops a second request to the same field from being acted on.
 */
@Immutable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmInline
value class FocusRequestToken(private val request: Any)
