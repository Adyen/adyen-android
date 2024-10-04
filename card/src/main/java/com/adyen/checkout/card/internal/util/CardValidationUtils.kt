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
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidator
import com.adyen.checkout.core.ui.validation.CardNumberValidationResult
import com.adyen.checkout.core.ui.validation.CardNumberValidator
import java.util.Calendar
import java.util.GregorianCalendar

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardValidationUtils {

    // Security Code
    private const val GENERAL_CARD_SECURITY_CODE_SIZE = 3
    private const val AMEX_SECURITY_CODE_SIZE = 4

    /**
     * Validate card number.
     */
    fun validateCardNumber(number: String, enableLuhnCheck: Boolean, isBrandSupported: Boolean): CardNumberValidation {
        val validation = CardNumberValidator.validateCardNumber(number, enableLuhnCheck)
        return when (validation) {
            CardNumberValidationResult.INVALID_ILLEGAL_CHARACTERS -> CardNumberValidation.INVALID_ILLEGAL_CHARACTERS
            CardNumberValidationResult.INVALID_TOO_LONG -> CardNumberValidation.INVALID_TOO_LONG
            CardNumberValidationResult.INVALID_TOO_SHORT -> CardNumberValidation.INVALID_TOO_SHORT
            CardNumberValidationResult.INVALID_LUHN_CHECK -> CardNumberValidation.INVALID_LUHN_CHECK
            CardNumberValidationResult.VALID -> when {
                !isBrandSupported -> CardNumberValidation.INVALID_UNSUPPORTED_BRAND
                else -> CardNumberValidation.VALID
            }
        }
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
            CardExpiryDateValidator.validateExpiryDate(expiryDate, calendar)
        val validation = generateExpiryDateValidation(fieldPolicy, expiryDateValidationResult)

        return FieldState(expiryDate, validation)
    }

    @VisibleForTesting
    internal fun generateExpiryDateValidation(
        fieldPolicy: Brand.FieldPolicy?,
        expiryDateValidationResult: CardExpiryDateValidationResult,
    ): Validation {
        return when (expiryDateValidationResult) {
            CardExpiryDateValidationResult.VALID -> Validation.Valid

            CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_far_in_future)

            CardExpiryDateValidationResult.INVALID_TOO_OLD ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_old)

            CardExpiryDateValidationResult.INVALID_DATE_FORMAT ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid)

            CardExpiryDateValidationResult.INVALID_OTHER_REASON -> if (fieldPolicy?.isRequired() == false) {
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardNumberValidation {
    VALID,
    INVALID_ILLEGAL_CHARACTERS,
    INVALID_LUHN_CHECK,
    INVALID_TOO_SHORT,
    INVALID_TOO_LONG,
    INVALID_UNSUPPORTED_BRAND
}
