/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/4/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DateUtils {
    /**
     * @param date A date string (21/12/31)
     * @param pattern A date format string (e.g. YYMMDD)
     *
     * @return Whether given [date] matches given [pattern]
     */
    fun matchesFormat(date: String, pattern: String): Boolean {
        return parseToDate(date, pattern) != null
    }

    /**
     * @param date A date string (21/12/31)
     * @param pattern A date format string (e.g. YYMMDD)
     *
     * @return the parsed date or null if the date does not match the given [pattern]
     */
    fun parseToDate(date: String, pattern: String): Date? {
        val formatter = SimpleDateFormat(pattern, Locale.ROOT).apply {
            isLenient = false
        }
        return parseToDate(date, formatter)
    }

    /**
     * @param date A date string (21/12/31)
     * @param formatter A SimpleDateFormat
     *
     * @return Whether given [date] matches given formatter's pattern
     */
    fun matchesFormat(date: String, formatter: SimpleDateFormat): Boolean {
        return parseToDate(date, formatter) != null
    }

    /**
     * @param date A date string (21/12/31)
     * @param formatter A SimpleDateFormat
     *
     * @return the parsed date or null if the date does not match the given formatter's pattern
     */
    fun parseToDate(date: String, formatter: SimpleDateFormat): Date? {
        return try {
            formatter.parse(date)
        } catch (_: ParseException) {
            adyenLog(AdyenLogLevel.VERBOSE) {
                "Provided date $date does not match the given format ${formatter.toPattern()}"
            }
            null
        }
    }

    fun getFormatter(pattern: String): SimpleDateFormat {
        return SimpleDateFormat(pattern, Locale.ROOT).apply {
            isLenient = false
        }
    }
}
