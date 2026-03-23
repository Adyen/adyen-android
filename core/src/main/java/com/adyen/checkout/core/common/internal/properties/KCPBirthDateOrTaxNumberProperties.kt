/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/3/2026.
 */

package com.adyen.checkout.core.common.internal.properties

import androidx.annotation.RestrictTo

/**
 * This field has two valid formats:
 * - Birth date: 6 digits, following the "yyMMdd" format e.g 230704 for 4 July 2023
 * - Tax number: 10 digits, no special format
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object KCPBirthDateOrTaxNumberProperties {

    const val KCP_BIRTH_DATE_VALID_LENGTH = 6
    const val KCP_TAX_NUMBER_VALID_LENGTH = 10

    // same as the tax number (the longer format)
    const val KCP_BIRTH_DATE_OR_TAX_NUMBER_MAX_LENGTH = 10
}
