/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/3/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties.EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS

internal object ExpiryDateParser {
    /**
     * Parses digit only input into a pair of expiryMonth and expiryYear
     * The input must be 4 digits long, 2-digit month followed by 2-digit year (MMYY)
     * Automatically adds the year prefix if returnFullYear is true (26 -> 2026)
     * Returns null if input is invalid
     */
    fun parseToMonthAndYear(expiryDate: String, returnFullYear: Boolean): Pair<String, String>? {
        if (expiryDate.length != EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS) {
            return null
        }
        val expiryMonth = expiryDate.take(2)
        val shortYear = expiryDate.takeLast(2)
        val expiryYear = if (returnFullYear) "$YEAR_PREFIX$shortYear" else shortYear

        return expiryMonth to expiryYear
    }

    private const val YEAR_PREFIX = "20"
}
