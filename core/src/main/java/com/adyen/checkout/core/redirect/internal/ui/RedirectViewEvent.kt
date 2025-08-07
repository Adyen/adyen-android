/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/8/2025.
 */

package com.adyen.checkout.core.redirect.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class RedirectViewEvent {
    data class Redirect(val url: String) : RedirectViewEvent()
}
