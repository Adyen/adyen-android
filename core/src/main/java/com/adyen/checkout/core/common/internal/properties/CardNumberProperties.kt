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
 * - Regular, formatted as such 1234 1234 1234 1234
 * - Amex, formatted as such 1234 123456 12345 1234
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardNumberProperties {

    const val CARD_NUMBER_MINIMUM_LENGTH = 12
    const val CARD_NUMBER_MAXIMUM_LENGTH = 19

    const val CARD_NUMBER_SEPARATOR = ' '
}
