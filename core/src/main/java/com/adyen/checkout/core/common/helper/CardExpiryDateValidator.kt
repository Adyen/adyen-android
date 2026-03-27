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
        currentCalendar: Calendar,
    ): CardExpiryDateValidationResult {
        val expiryDateCalendar = getExpiryCalendar(expiryMonth, expiryYear)
            ?: return CardExpiryDateValidationResult.Invalid.NonParseableDate()

        val isInMaxYearRange = isInMaxYearRange(expiryDateCalendar, currentCalendar)
        val isInMinMonthRange = isInMinMonthRange(expiryDateCalendar, currentCalendar)

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
            else -> getExpiryCalendar(month = month, year = year)
        }
    }

    private fun getExpiryCalendar(
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
        }
    }

    /**
     * Returns true if the year of the card expiry date is not more than 30 years in the future from ths year.
     */
    private fun isInMaxYearRange(expiryDateCalendar: Calendar, currentCalendar: Calendar): Boolean {
        val expiryDateYear = expiryDateCalendar.get(Calendar.YEAR)
        val currentYear = currentCalendar.get(Calendar.YEAR)

        // we only compare years when validating future
        return currentYear + MAXIMUM_YEARS_IN_FUTURE >= expiryDateYear
    }

    /**
     * Returns true if the card has not expired more than MAXIMUM_EXPIRED_MONTHS complete months ago.
     * The current month is fully ignored, we only count complete months in the past
     */
    private fun isInMinMonthRange(expiryDateCalendar: Calendar, currentCalendar: Calendar): Boolean {
        val earliestInvalidCalendar = (expiryDateCalendar.clone() as Calendar).apply {
            add(Calendar.MONTH, MAXIMUM_MONTHS_IN_PAST)

            // since we only count complete months in the past, we go to next month to be on the actual first invalid
            // moment of the expiry date
            // e.g. a card with expiry date 01/30 is valid until the last moment of April 2030 (30 April 23:59:59)
            // which is almost 4 months after the expiry date
            add(Calendar.MONTH, 1)
        }
        return currentCalendar < earliestInvalidCalendar
    }

    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_MONTHS_IN_PAST = 3
    private const val MIN_MONTH_VALUE = 1
    private const val MAX_MONTH_VALUE = 12
    private const val MIN_YEAR_VALUE = 0
    private const val MAX_YEAR_VALUE = 99
    private const val YEAR_2000 = 2000
}
