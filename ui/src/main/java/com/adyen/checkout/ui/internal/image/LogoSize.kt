/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/8/2025.
 */

package com.adyen.checkout.ui.internal.image

import androidx.annotation.RestrictTo

/**
 * The logo size.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class LogoSize {
    /**
     * Size for small logos (height: 26dp).
     */
    SMALL,

    /**
     * Size for medium logos (height: 50dp).
     */
    MEDIUM,

    /**
     * Size for large logos (height: 100dp).
     */
    LARGE;

    override fun toString(): String {
        return name.lowercase()
    }
}
