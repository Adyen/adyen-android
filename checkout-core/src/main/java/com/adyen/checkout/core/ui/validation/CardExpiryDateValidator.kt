/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.ui.validation

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.ui.model.ExpiryDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

object CardExpiryDateValidator {
    // Date
    private const val YEARS_IN_CENTURY = 100
    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_EXPIRED_MONTHS = 3
    private const val DATE_FORMAT = "MM/yy"

    private val dateFormat: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

    init {
        dateFormat.isLenient = false
    }

    /**
     * Validate expiry date.
     *
     * @param expiryDate Expiry date.
     *
     * @return Validation result.
     */
    fun validateExpiryDate(
        expiryDate: ExpiryDate
    ): CardExpiryDateValidationResult {
        return validateExpiryDate(expiryDate.toMMyyString(), GregorianCalendar.getInstance())
    }

    /**
     * Validate expiry date.
     *
     * @param expiryDate Expiry date in MM/yy format.
     *
     * @return Validation result.
     */
    fun validateExpiryDate(
        expiryDate: String
    ) = validateExpiryDate(expiryDate, GregorianCalendar.getInstance())

    @VisibleForTesting
    internal fun validateExpiryDate(
        expiryDate: String,
        calendar: Calendar
    ) = when {
        dateExists(expiryDate) -> {
            val expiryDateCalendar = getExpiryCalendar(expiryDate)
            val isInMaxYearRange = isInMaxYearRange(expiryDateCalendar, calendar)
            val isInMinMonthRange = isInMinMonthRange(expiryDateCalendar, calendar)

            when {
                // higher than maxPast and lower than maxFuture
                isInMinMonthRange && isInMaxYearRange -> CardExpiryDateValidationResult.Valid()
                !isInMaxYearRange -> CardExpiryDateValidationResult.Invalid.TooFarInTheFuture()
                // Too old (!isInMinMonthRange)
                else -> CardExpiryDateValidationResult.Invalid.TooOld()
            }
        }

        else -> CardExpiryDateValidationResult.Invalid.NonParseableDate()
    }

    private fun isInMaxYearRange(expiryDateCalendar: Calendar, calendar: Calendar): Boolean {
        val maxFutureCalendar = calendar.clone() as GregorianCalendar
        maxFutureCalendar.add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
        return expiryDateCalendar.get(Calendar.YEAR) <= maxFutureCalendar.get(Calendar.YEAR)
    }

    private fun isInMinMonthRange(expiryDateCalendar: Calendar, calendar: Calendar): Boolean {
        val maxPastCalendar = calendar.clone() as GregorianCalendar
        maxPastCalendar.add(Calendar.MONTH, -MAXIMUM_EXPIRED_MONTHS)
        return expiryDateCalendar >= maxPastCalendar
    }

    private fun dateExists(expiryDate: String): Boolean {
        return try {
            dateFormat.parse(expiryDate) != null
        } catch (e: ParseException) {
            adyenLog(AdyenLogLevel.WARN) { "Invalid expiry date: $expiryDate" }
            false
        }
    }

    private fun getExpiryCalendar(expiryDate: String): Calendar {
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
        // On SimpleDateFormat, if the truncated (yy) year is more than 20 years in the future it will use the previous
        // century.
        // This is a small fix to correct for that without implementing or overriding the DateFormat class.
        val currentCalendar = GregorianCalendar.getInstance().apply {
            // Add the max expiry years, so that when the next century approaches dates in the next century are used.
            add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
        }
        val currentCentury = currentCalendar[Calendar.YEAR] / YEARS_IN_CENTURY
        val calendarCentury = calendar[Calendar.YEAR] / YEARS_IN_CENTURY
        if (calendarCentury < currentCentury) {
            calendar.add(Calendar.YEAR, YEARS_IN_CENTURY)
        }
    }
}
