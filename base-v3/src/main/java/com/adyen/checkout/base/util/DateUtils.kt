/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/11/2020.
 */

package com.adyen.checkout.base.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateUtils private constructor() {
    companion object {
        @JvmStatic
        fun parseDateToView(month: String, year: String): String {
            // Refactor this to DateFormat if we need to localize.
            return if (year.length < 2) "$month/$year"
            else "$month/${year.substring(0..2)}"
        }

        /**
         * Convert to server date format.
         */
        @JvmStatic
        fun toServerDateFormat(calendar: Calendar): String {
            val serverDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return serverDateFormat.format(calendar.time)
        }
    }
}
