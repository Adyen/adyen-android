/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.common.internal.helper.DateUtils
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
        // this manual check is needed because SimpleDateFormat.parse will not enforce the length and will
        // successfully parse some months and years that are not 2 digits long
        if (expiryMonth.length != MONTH_YEAR_NUMBER_OF_DIGITS || expiryYear.length != MONTH_YEAR_NUMBER_OF_DIGITS) {
            return null
        }

        val date = DateUtils.parseToDate("$expiryMonth$expiryYear", PARSING_FORMATTER)
        return date?.let {
            GregorianCalendar().apply {
                // Clear all fields to avoid unexpected results from current time
                clear()
                time = date
            }
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
    private const val MONTH_YEAR_NUMBER_OF_DIGITS = 2

    // instantiate formatter here to avoid recreation on every validation call
    private val PARSING_FORMATTER = DateUtils.getFormatter("MMyy").apply {
        // if the expiryYear is more than 20 years in the future, SimpleDateFormat will use the previous century
        // which means "52" gets parsed to 1952 instead of 2052
        // this call ensures that every parsed date is after the year 2000
        set2DigitYearStart(GregorianCalendar(2000, 0, 1).time)
    }
}
