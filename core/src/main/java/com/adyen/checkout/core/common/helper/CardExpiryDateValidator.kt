/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.VisibleForTesting
import java.util.Calendar
import java.util.GregorianCalendar

object CardExpiryDateValidator {
    /**
     * Expiry date.
     *
     * @param expiryMonth 2-digit month value. Valid values are [01-12], 01 being January and 12 being December.
     * @param expiryYear 2-digit year valid. Valid values are [00-99], 00 being 2000 and 99 being 2099.
     */
    fun validateExpiryDate(
        expiryMonth: String,
        expiryYear: String,
    ): CardExpiryDateValidationResult {
        return validateExpiryDate(expiryMonth, expiryYear, GregorianCalendar.getInstance())
    }

    @VisibleForTesting
    internal fun validateExpiryDate(
        expiryMonth: String,
        expiryYear: String,
        calendar: Calendar,
    ): CardExpiryDateValidationResult {
        val expiryDateCalendar = getExpiryCalendar(expiryMonth, expiryYear)
            ?: return CardExpiryDateValidationResult.Invalid.NonParseableDate()

        val isInMaxYearRange = isInMaxYearRange(expiryDateCalendar, calendar)
        val isInMinMonthRange = isInMinMonthRange(expiryDateCalendar, calendar)

        return when {
            !isInMaxYearRange -> CardExpiryDateValidationResult.Invalid.TooFarInTheFuture()
            !isInMinMonthRange -> CardExpiryDateValidationResult.Invalid.TooOld()
            else -> CardExpiryDateValidationResult.Valid()
        }
    }

    private fun getExpiryCalendar(
        expiryMonth: String,
        expiryYear: String,
    ): Calendar? {
        val month = expiryMonth.toIntOrNull()
        val year = expiryYear.toIntOrNull()
        return when {
            month == null -> null
            year == null -> null
            month !in MIN_MONTH_VALUE..MAX_MONTH_VALUE -> null
            year !in MIN_YEAR_VALUE..MAX_YEAR_VALUE -> null
            else -> getCalendar(month = month, year = year)
        }
    }

    private fun getCalendar(
        month: Int,
        year: Int,
    ): Calendar {
        return GregorianCalendar().apply {
            // Clear all fields to avoid unexpected results from current time
            clear()
            // Calendar months are 0-based (January is 0), so we subtract 1
            set(Calendar.MONTH, month - 1)
            // add 2000 to the 2-digit year
            set(Calendar.YEAR, YEAR_2000 + year)
            // Go to next month and remove 1 day to be on the last day of the expiry month.
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
        }
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

    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_EXPIRED_MONTHS = 3
    private const val MIN_MONTH_VALUE = 1
    private const val MAX_MONTH_VALUE = 12
    private const val MIN_YEAR_VALUE = 0
    private const val MAX_YEAR_VALUE = 99
    private const val YEAR_2000 = 2000
}
