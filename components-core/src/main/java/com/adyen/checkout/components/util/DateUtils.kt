/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/11/2020.
 */

package com.adyen.checkout.components.util

import com.adyen.checkout.core.log.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

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
            Logger.e("DateUtil", "Provided date $date does not match the given format $format")
            false
        }
    }
}
