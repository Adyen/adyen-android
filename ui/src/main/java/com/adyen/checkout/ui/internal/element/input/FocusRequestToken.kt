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
 * A pending request to move focus to a field, as far as the field itself is concerned.
 *
 * The state layer decides which field should receive focus and why, but it lives in a module this one cannot see, so
 * the request arrives here as an opaque value. All a field needs from it is to tell one request from the next, which
 * is why the wrapped value is not readable: wrap whatever the state layer uses to describe the request, and make sure
 * it is comparable by equality.
 *
 * **Only wrap an immutable value.** [Any] tells the Compose compiler nothing, so without the [Immutable] promise below
 * every view state holding a token would be treated as unstable, and every field would recompose on every keystroke.
 * The annotation is what buys that back, and it is only true if callers keep their end of the bargain.
 */
@Immutable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmInline
value class FocusRequestToken(private val request: Any)
