/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.card.util

import com.adyen.checkout.card.R
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.util.StringUtil
import java.util.Calendar
import java.util.GregorianCalendar

object CardValidationUtils {

    // Luhn Check
    private const val RADIX = 10
    private const val FIVE_DIGIT = 5

    // Card Number
    private const val MINIMUM_CARD_NUMBER_LENGTH = 8
    const val MAXIMUM_CARD_NUMBER_LENGTH = 19

    // Security Code
    private const val GENERAL_CARD_SECURITY_CODE_SIZE = 3
    private const val AMEX_SECURITY_CODE_SIZE = 4

    // Date
    private const val MONTHS_IN_YEAR = 12
    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_EXPIRED_MONTHS = 3

    /**
     * Validate card number.
     */
    fun validateCardNumber(number: String, enableLuhnCheck: Boolean, isBrandSupported: Boolean): CardNumberValidation {
        val normalizedNumber = StringUtil.normalize(number)
        val length = normalizedNumber.length
        return when {
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedNumber) -> CardNumberValidation.INVALID_ILLEGAL_CHARACTERS
            length > MAXIMUM_CARD_NUMBER_LENGTH -> CardNumberValidation.INVALID_TOO_LONG
            length < MINIMUM_CARD_NUMBER_LENGTH -> CardNumberValidation.INVALID_TOO_SHORT
            !isBrandSupported -> CardNumberValidation.INVALID_UNSUPPORTED_BRAND
            enableLuhnCheck && !isLuhnChecksumValid(normalizedNumber) -> CardNumberValidation.INVALID_LUHN_CHECK
            else -> CardNumberValidation.VALID
        }
    }

    @Suppress("MagicNumber")
    private fun isLuhnChecksumValid(normalizedNumber: String): Boolean {
        var s1 = 0
        var s2 = 0
        val reverse = StringBuffer(normalizedNumber).reverse().toString()
        for (i in reverse.indices) {
            val digit = Character.digit(reverse[i], RADIX)
            if (i % 2 == 0) {
                s1 += digit
            } else {
                s2 += 2 * digit
                if (digit >= FIVE_DIGIT) {
                    s2 -= 9
                }
            }
        }
        return (s1 + s2) % 10 == 0
    }

    /**
     * Validate Expiry Date.
     */
    fun validateExpiryDate(expiryDate: ExpiryDate, fieldPolicy: Brand.FieldPolicy?): FieldState<ExpiryDate> {
        val invalidState = FieldState(expiryDate, Validation.Invalid(R.string.checkout_expiry_date_not_valid))
        return when {
            dateExists(expiryDate) -> {
                val isInMaxYearRange = isInMaxYearRange(expiryDate, GregorianCalendar.getInstance())
                val isInMinMonthRange = isInMinMonthRange(expiryDate, GregorianCalendar.getInstance())
                val fieldState = when {
                    // higher than maxPast and lower than maxFuture
                    isInMinMonthRange && isInMaxYearRange -> FieldState(expiryDate, Validation.Valid)
                    !isInMaxYearRange -> FieldState(
                        expiryDate,
                        Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_far_in_future)
                    )
                    !isInMinMonthRange -> FieldState(
                        expiryDate,
                        Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_old)
                    )
                    else -> invalidState
                }
                fieldState
            }
            (fieldPolicy == Brand.FieldPolicy.OPTIONAL || fieldPolicy == Brand.FieldPolicy.HIDDEN)
                && expiryDate != ExpiryDate.INVALID_DATE -> {
                FieldState(expiryDate, Validation.Valid)
            }
            else -> invalidState
        }
    }

    internal fun isInMaxYearRange(expiryDate: ExpiryDate, calendar: Calendar): Boolean {
        val expiryDateCalendar = getExpiryCalendar(expiryDate)
        val maxFutureCalendar = calendar.clone() as GregorianCalendar
        maxFutureCalendar.add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
        return expiryDateCalendar.get(Calendar.YEAR) <= maxFutureCalendar.get(Calendar.YEAR)
    }

    internal fun isInMinMonthRange(expiryDate: ExpiryDate, calendar: Calendar): Boolean {
        val expiryDateCalendar = getExpiryCalendar(expiryDate)
        val maxPastCalendar = calendar.clone() as GregorianCalendar
        maxPastCalendar.add(Calendar.MONTH, -MAXIMUM_EXPIRED_MONTHS)
        return expiryDateCalendar >= maxPastCalendar
    }

    /**
     * Validate Security Code.
     */
    fun validateSecurityCode(securityCode: String, cardType: DetectedCardType?): FieldState<String> {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length
        val invalidState = Validation.Invalid(R.string.checkout_security_code_not_valid)
        val validation = when {
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedSecurityCode) -> invalidState
            cardType?.cvcPolicy == Brand.FieldPolicy.OPTIONAL && length == 0 -> Validation.Valid
            cardType?.cardType == CardType.AMERICAN_EXPRESS && length == AMEX_SECURITY_CODE_SIZE -> Validation.Valid
            cardType?.cardType != CardType.AMERICAN_EXPRESS
                && length == GENERAL_CARD_SECURITY_CODE_SIZE -> Validation.Valid
            else -> invalidState
        }
        return FieldState(normalizedSecurityCode, validation)
    }

    private fun dateExists(expiryDate: ExpiryDate): Boolean {
        return (
            expiryDate !== ExpiryDate.EMPTY_DATE &&
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

enum class CardNumberValidation {
    VALID,
    INVALID_ILLEGAL_CHARACTERS,
    INVALID_LUHN_CHECK,
    INVALID_TOO_SHORT,
    INVALID_TOO_LONG,
    INVALID_UNSUPPORTED_BRAND
}
