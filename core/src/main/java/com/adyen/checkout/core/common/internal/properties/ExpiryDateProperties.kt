/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/3/2026.
 */

package com.adyen.checkout.core.common.internal.properties

import androidx.annotation.RestrictTo

/**
 * This field is formatted as such 03/30
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ExpiryDateProperties {
    const val EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS = 4
    const val EXPIRY_DATE_SEPARATOR = '/'
}
