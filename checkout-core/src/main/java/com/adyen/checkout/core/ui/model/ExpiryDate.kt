/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */
package com.adyen.checkout.core.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.ui.model.INVALID_DATE
import com.adyen.checkout.core.internal.ui.model.toMMyyString
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Expiry date.
 *
 * @param expiryMonth 1 based month value. Valid values are [1-12], 1 being January and 12 being December.
 * @param expiryYear 4 digit year (i.e. 2024)
 */
data class ExpiryDate(
    val expiryMonth: Int,
    val expiryYear: Int,
) {

    /**
     * Convert this instance to a date string with the "MM/yy" format.
     */
    fun toMMyyString(): String = toMMyyString(expiryMonth.toString(), expiryYear.toString())

    companion object {

        private const val YEARS_IN_CENTURY = 100
        internal const val MAXIMUM_YEARS_IN_FUTURE = 30
        internal const val MAXIMUM_EXPIRED_MONTHS = 3
        private const val DATE_FORMAT = "MM/yy"
        private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

        init {
            dateFormat.isLenient = false
        }

        /**
         * Create an [ExpiryDate] from a string, expecting the `MM/yy` date format
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun from(expiryDate: String): ExpiryDate {
            return try {
                val calendar = getExpiryCalendar(expiryDate)
                // Correct Calendar's zero based month
                ExpiryDate(calendar[Calendar.MONTH] + 1, calendar[Calendar.YEAR])
            } catch (e: ParseException) {
                INVALID_DATE
            }
        }

        internal fun getExpiryCalendar(expiryDate: String): Calendar {
            val parsedDate = requireNotNull(dateFormat.parse(expiryDate))
            val expiryCalendar = GregorianCalendar.getInstance()
            expiryCalendar.time = parsedDate
            fixCalendarYear(expiryCalendar)
            // Go to next month and remove 1 day to be on the last day of the expiry month.
            expiryCalendar.add(Calendar.MONTH, 1)
            expiryCalendar.add(Calendar.DAY_OF_MONTH, -1)
            return expiryCalendar
        }

        private fun fixCalendarYear(calendar: Calendar) {
            // On SimpleDateFormat, if the truncated (yy) year is more than 20 years in the future it will use the
            // previous century.
            // This is a small fix to correct for that without implementing or overriding the DateFormat class.
            val currentCalendar = GregorianCalendar.getInstance().apply {
                // Add the max expiry years, so that when the next century approaches dates in the next century are
                // used.
                add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
            }
            val currentCentury = currentCalendar[Calendar.YEAR] / YEARS_IN_CENTURY
            val calendarCentury = calendar[Calendar.YEAR] / YEARS_IN_CENTURY
            if (calendarCentury < currentCentury) {
                calendar.add(Calendar.YEAR, YEARS_IN_CENTURY)
            }
        }
    }
}
