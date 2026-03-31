/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/3/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties.EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object ExpiryDateParser {
    private const val MONTH_YEAR_FORMAT = "MMyy"
    private const val MONTH_FORMAT = "MM"
    private const val SHORT_YEAR_FORMAT = "yy"
    private const val FULL_YEAR_FORMAT = "yyyy"

    /**
     * Parses digit only input into a pair of expiryMonth and expiryYear
     * The input must be 4 digits long, 2-digit month followed by 2-digit year (MMYY)
     * Automatically adds the year prefix if returnFullYear is true (26 -> 2026)
     * Returns null if input is invalid
     */
    fun parseToMonthAndYear(expiryDate: String, returnFullYear: Boolean): Pair<String, String>? {
        // this manual check is needed because SimpleDateFormat.parse will not enforce the length and will successfully
        // parse some strings that are not 4 digits long
        if (expiryDate.length != EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS) {
            return null
        }

        val date = parseToDate(expiryDate)
        return date?.let {
            getMonthAndYear(date, returnFullYear)
        }
    }

    private fun parseToDate(expiryDate: String): Date? {
        return try {
            val parsingFormatter = getFormatter(MONTH_YEAR_FORMAT)
            parsingFormatter.parse(expiryDate)
        } catch (_: ParseException) {
            null
        }
    }

    private fun getMonthAndYear(date: Date, returnFullYear: Boolean): Pair<String, String> {
        val monthFormatter = getFormatter(MONTH_FORMAT)
        val yearFormat = if (returnFullYear) FULL_YEAR_FORMAT else SHORT_YEAR_FORMAT
        val yearFormatter = getFormatter(yearFormat)

        return monthFormatter.format(date) to yearFormatter.format(date)
    }

    private fun getFormatter(format: String): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.ROOT).apply {
            isLenient = false
        }
    }
}
