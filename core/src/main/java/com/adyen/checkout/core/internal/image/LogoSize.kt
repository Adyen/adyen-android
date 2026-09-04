/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/8/2025.
 */

package com.adyen.checkout.core.internal.image

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class LogoSize(val value: String) {
    /**
     * Size for small logos (height: 26dp).
     */
    SMALL("small"),

    /**
     * Size for medium logos (height: 50dp).
     */
    MEDIUM("medium"),

    /**
     * Size for large logos (height: 100dp).
     */
    LARGE("large"),
}
