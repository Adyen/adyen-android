/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.ui.model.ExpiryDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

object CardExpiryDateValidator {
    // Date
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
            val expiryDateCalendar = ExpiryDate.getExpiryCalendar(expiryDate)
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
        maxFutureCalendar.add(Calendar.YEAR, ExpiryDate.MAXIMUM_YEARS_IN_FUTURE)
        return expiryDateCalendar.get(Calendar.YEAR) <= maxFutureCalendar.get(Calendar.YEAR)
    }

    private fun isInMinMonthRange(expiryDateCalendar: Calendar, calendar: Calendar): Boolean {
        val maxPastCalendar = calendar.clone() as GregorianCalendar
        maxPastCalendar.add(Calendar.MONTH, -ExpiryDate.MAXIMUM_EXPIRED_MONTHS)
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
}
