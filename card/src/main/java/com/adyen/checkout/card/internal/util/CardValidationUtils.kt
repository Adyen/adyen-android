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
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.core.old.internal.ui.model.isEmptyDate
import com.adyen.checkout.core.old.internal.util.StringUtil
import com.adyen.checkout.core.old.ui.model.ExpiryDate
import com.adyen.checkout.core.old.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.old.ui.validation.CardExpiryDateValidator
import com.adyen.checkout.core.old.ui.validation.CardNumberValidationResult
import com.adyen.checkout.core.old.ui.validation.CardNumberValidator
import com.adyen.checkout.core.old.ui.validation.CardSecurityCodeValidationResult
import com.adyen.checkout.core.old.ui.validation.CardSecurityCodeValidator

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardValidationUtils {

    /**
     * Validate card number.
     */
    internal fun validateCardNumber(
        number: String,
        enableLuhnCheck: Boolean,
        isBrandSupported: Boolean
    ): CardNumberValidation {
        val validation = CardNumberValidator.validateCardNumber(number, enableLuhnCheck)
        return validateCardNumber(validation, isBrandSupported)
    }

    @VisibleForTesting
    internal fun validateCardNumber(
        validationResult: CardNumberValidationResult,
        isBrandSupported: Boolean
    ): CardNumberValidation {
        return when (validationResult) {
            is CardNumberValidationResult.Invalid -> {
                when (validationResult) {
                    is CardNumberValidationResult.Invalid.IllegalCharacters ->
                        CardNumberValidation.INVALID_ILLEGAL_CHARACTERS
                    is CardNumberValidationResult.Invalid.TooLong -> CardNumberValidation.INVALID_TOO_LONG
                    is CardNumberValidationResult.Invalid.TooShort -> CardNumberValidation.INVALID_TOO_SHORT
                    is CardNumberValidationResult.Invalid.LuhnCheck -> CardNumberValidation.INVALID_LUHN_CHECK
                    else -> {
                        CardNumberValidation.INVALID_OTHER_REASON
                    }
                }
            }
            is CardNumberValidationResult.Valid -> when {
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
    ): CardExpiryDateValidation {
        val result = CardExpiryDateValidator.validateExpiryDate(expiryDate)
        return validateExpiryDate(expiryDate, result, fieldPolicy)
    }

    @VisibleForTesting
    internal fun validateExpiryDate(
        expiryDate: ExpiryDate,
        validationResult: CardExpiryDateValidationResult,
        fieldPolicy: Brand.FieldPolicy?
    ): CardExpiryDateValidation {
        return when (validationResult) {
            is CardExpiryDateValidationResult.Valid -> CardExpiryDateValidation.VALID

            is CardExpiryDateValidationResult.Invalid -> {
                when (validationResult) {
                    is CardExpiryDateValidationResult.Invalid.TooFarInTheFuture ->
                        CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE

                    is CardExpiryDateValidationResult.Invalid.TooOld ->
                        CardExpiryDateValidation.INVALID_TOO_OLD

                    is CardExpiryDateValidationResult.Invalid.NonParseableDate -> {
                        if (expiryDate.isEmptyDate() && fieldPolicy?.isRequired() == false) {
                            CardExpiryDateValidation.VALID_NOT_REQUIRED
                        } else {
                            CardExpiryDateValidation.INVALID_OTHER_REASON
                        }
                    }

                    else -> {
                        // should not happen, due to CardExpiryDateValidationResult being an abstract class
                        CardExpiryDateValidation.INVALID_OTHER_REASON
                    }
                }
            }
        }
    }

    /**
     * Validate Security Code.
     */
    internal fun validateSecurityCode(
        securityCode: String,
        detectedCardType: DetectedCardType?,
        uiState: InputFieldUIState
    ): CardSecurityCodeValidation {
        val result = CardSecurityCodeValidator.validateSecurityCode(securityCode, detectedCardType?.cardBrand)
        return validateSecurityCode(securityCode, uiState, result)
    }

    @VisibleForTesting
    internal fun validateSecurityCode(
        securityCode: String,
        uiState: InputFieldUIState,
        validationResult: CardSecurityCodeValidationResult
    ): CardSecurityCodeValidation {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length

        return when {
            uiState == InputFieldUIState.HIDDEN -> CardSecurityCodeValidation.VALID_HIDDEN
            uiState == InputFieldUIState.OPTIONAL && length == 0 -> CardSecurityCodeValidation.VALID_OPTIONAL_EMPTY
            else -> {
                when (validationResult) {
                    is CardSecurityCodeValidationResult.Invalid -> CardSecurityCodeValidation.INVALID
                    is CardSecurityCodeValidationResult.Valid -> CardSecurityCodeValidation.VALID
                }
            }
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardNumberValidation {
    VALID,
    INVALID_ILLEGAL_CHARACTERS,
    INVALID_LUHN_CHECK,
    INVALID_TOO_SHORT,
    INVALID_TOO_LONG,
    INVALID_UNSUPPORTED_BRAND,
    INVALID_OTHER_REASON
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardExpiryDateValidation {
    VALID,
    VALID_NOT_REQUIRED,
    INVALID_TOO_FAR_IN_THE_FUTURE,
    INVALID_TOO_OLD,
    INVALID_OTHER_REASON,
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardSecurityCodeValidation {
    VALID,
    VALID_HIDDEN,
    VALID_OPTIONAL_EMPTY,
    INVALID,
}
