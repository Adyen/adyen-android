/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/11/2020.
 */

package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DateUtils {
    private const val DEFAULT_INPUT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    private val TAG = LogUtil.getTag()

    @JvmStatic
    fun parseDateToView(month: String, year: String): String {
        // Refactor this to DateFormat if we need to localize.
        return "$month/${year.takeLast(2)}"
    }

    /**
     * Convert to server date format.
     */
    @JvmStatic
    fun toServerDateFormat(calendar: Calendar): String {
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return serverDateFormat.format(calendar.time)
    }

    /**
     * @param date A date string (21/12/31)
     * @param format A date format string (e.g. YYMMDD)
     *
     * @return Whether given [date] matches given [format]
     */
    fun matchesFormat(date: String, format: String): Boolean {
        val dateFormat = SimpleDateFormat(format, Locale.US)
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date)
            true
        } catch (e: ParseException) {
            Logger.e(TAG, "Provided date $date does not match the given format $format")
            false
        }
    }

    /**
     * Format server date pattern to regular date pattern (30/03/2023).
     *
     * @param date date value coming from server
     * @param shopperLocale
     * @param inputFormat server date pattern
     */
    fun formatStringDate(
        date: String,
        shopperLocale: Locale,
        inputFormat: String = DEFAULT_INPUT_DATE_FORMAT
    ): String? {
        return try {
            val inputSimpleFormat = SimpleDateFormat(inputFormat, shopperLocale)
            val outputSimpleFormat = DateFormat.getDateInstance(DateFormat.SHORT, shopperLocale)
            val parsedDate = inputSimpleFormat.parse(date)
            parsedDate?.let { outputSimpleFormat.format(it) }
        } catch (e: ParseException) {
            Logger.e(TAG, "Provided date $date does not match the given format $inputFormat")
            null
        }
    }
}
