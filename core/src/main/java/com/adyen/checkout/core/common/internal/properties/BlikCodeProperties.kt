/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.core.common.internal.properties

import androidx.annotation.RestrictTo

/**
 * This field is formatted as such 123 456
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object BlikCodeProperties {
    const val BLIK_CODE_MAX_LENGTH = 6
    const val BLIK_CODE_SEPARATOR = ' '
}
