/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.ui.validation

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.internal.ui.model.isEmptyDate
import com.adyen.checkout.core.internal.ui.model.isInvalidDate
import com.adyen.checkout.core.ui.model.ExpiryDate
import java.util.Calendar
import java.util.GregorianCalendar

object CardExpiryDateValidator {
    // Date
    private const val MONTHS_IN_YEAR = 12
    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_EXPIRED_MONTHS = 3

    /**
     * Validate expiry date.
     *
     * @param expiryDate Expiry date.
     *
     * @return Validation result.
     */
    fun validateExpiryDate(
        expiryDate: ExpiryDate
    ) = validateExpiryDate(expiryDate, GregorianCalendar.getInstance())

    @VisibleForTesting
    internal fun validateExpiryDate(
        expiryDate: ExpiryDate,
        calendar: Calendar
    ) = when {
        dateExists(expiryDate) -> {
            val isInMaxYearRange = isInMaxYearRange(expiryDate, calendar)
            val isInMinMonthRange = isInMinMonthRange(expiryDate, calendar)

            when {
                // higher than maxPast and lower than maxFuture
                isInMinMonthRange && isInMaxYearRange -> CardExpiryDateValidationResult.VALID
                !isInMaxYearRange -> CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE
                // Too old (!isInMinMonthRange)
                else -> CardExpiryDateValidationResult.INVALID_TOO_OLD
            }
        }

        expiryDate.isInvalidDate() -> CardExpiryDateValidationResult.INVALID_DATE_FORMAT

        else -> CardExpiryDateValidationResult.INVALID_OTHER_REASON
    }

    private fun isInMaxYearRange(expiryDate: ExpiryDate, calendar: Calendar): Boolean {
        val expiryDateCalendar = getExpiryCalendar(expiryDate)
        val maxFutureCalendar = calendar.clone() as GregorianCalendar
        maxFutureCalendar.add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
        return expiryDateCalendar.get(Calendar.YEAR) <= maxFutureCalendar.get(Calendar.YEAR)
    }

    private fun isInMinMonthRange(expiryDate: ExpiryDate, calendar: Calendar): Boolean {
        val expiryDateCalendar = getExpiryCalendar(expiryDate)
        val maxPastCalendar = calendar.clone() as GregorianCalendar
        maxPastCalendar.add(Calendar.MONTH, -MAXIMUM_EXPIRED_MONTHS)
        return expiryDateCalendar >= maxPastCalendar
    }

    private fun dateExists(expiryDate: ExpiryDate): Boolean {
        return (
            !expiryDate.isEmptyDate() &&
                isValidMonth(expiryDate.expiryMonth) &&
                expiryDate.expiryYear > 0
            )
    }

    private fun isValidMonth(month: Int): Boolean {
        return month in 1..MONTHS_IN_YEAR
    }

    private fun getExpiryCalendar(expiryDate: ExpiryDate): Calendar {
        val expiryCalendar = GregorianCalendar.getInstance()
        expiryCalendar.clear()
        // First day of the expiry month. Calendar.MONTH is zero-based.
        expiryCalendar[expiryDate.expiryYear, expiryDate.expiryMonth - 1] = 1
        // Go to next month and remove 1 day to be on the last day of the expiry month.
        expiryCalendar.add(Calendar.MONTH, 1)
        expiryCalendar.add(Calendar.DAY_OF_MONTH, -1)
        return expiryCalendar
    }
}
