/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.card.internal.util

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.internal.util.StringUtil
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.util.ExpiryDateValidationResult
import com.adyen.checkout.ui.core.internal.util.ExpiryDateValidationUtils
import java.util.Calendar
import java.util.GregorianCalendar

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardValidationUtils {

    // Luhn Check
    private const val RADIX = 10
    private const val FIVE_DIGIT = 5

    // Card Number
    private const val MINIMUM_CARD_NUMBER_LENGTH = 12
    const val MAXIMUM_CARD_NUMBER_LENGTH = 19

    // Security Code
    private const val GENERAL_CARD_SECURITY_CODE_SIZE = 3
    private const val AMEX_SECURITY_CODE_SIZE = 4

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
    internal fun validateExpiryDate(
        expiryDate: ExpiryDate,
        fieldPolicy: Brand.FieldPolicy?,
        calendar: Calendar = GregorianCalendar.getInstance()
    ): FieldState<ExpiryDate> {
        val expiryDateValidationResult =
            ExpiryDateValidationUtils.validateExpiryDate(expiryDate, calendar)
        val validation = generateExpiryDateValidation(fieldPolicy, expiryDateValidationResult)

        return FieldState(expiryDate, validation)
    }

    @VisibleForTesting
    internal fun generateExpiryDateValidation(
        fieldPolicy: Brand.FieldPolicy?,
        expiryDateValidationResult: ExpiryDateValidationResult,
    ): Validation {
        return when (expiryDateValidationResult) {
            is ExpiryDateValidationResult.Valid -> Validation.Valid
            is ExpiryDateValidationResult.InvalidTooFarInTheFuture ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_far_in_future)

            is ExpiryDateValidationResult.InvalidTooOld ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_old)

            is ExpiryDateValidationResult.InvalidExpiryDate ->
                if (fieldPolicy?.isRequired() == false && !expiryDateValidationResult.isDateFormatInvalid) {
                    Validation.Valid
                } else {
                    Validation.Invalid(R.string.checkout_expiry_date_not_valid)
                }
        }
    }

    /**
     * Validate Security Code.
     */
    internal fun validateSecurityCode(
        securityCode: String,
        detectedCardType: DetectedCardType?,
        cvcUIState: InputFieldUIState
    ): FieldState<String> {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length
        val invalidState = Validation.Invalid(R.string.checkout_security_code_not_valid)
        val validation = when {
            cvcUIState == InputFieldUIState.HIDDEN -> Validation.Valid
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedSecurityCode) -> invalidState
            cvcUIState == InputFieldUIState.OPTIONAL && length == 0 -> Validation.Valid
            detectedCardType?.cardBrand == CardBrand(cardType = CardType.AMERICAN_EXPRESS) &&
                length == AMEX_SECURITY_CODE_SIZE -> Validation.Valid

            detectedCardType?.cardBrand != CardBrand(cardType = CardType.AMERICAN_EXPRESS) &&
                length == GENERAL_CARD_SECURITY_CODE_SIZE -> Validation.Valid

            else -> invalidState
        }
        return FieldState(normalizedSecurityCode, validation)
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
