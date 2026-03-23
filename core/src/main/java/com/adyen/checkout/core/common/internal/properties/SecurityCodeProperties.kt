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
 * This field has two valid formats:
 * - Regular, 3 digits
 * - Amex, 4 digits
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SecurityCodeProperties {
    const val SECURITY_CODE_MAX_LENGTH_DEFAULT = 3
    const val SECURITY_CODE_MAX_LENGTH_AMEX = 4
}
