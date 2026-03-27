/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.helper.ExpiryDateParser
import com.adyen.checkout.card.internal.ui.properties.KCPBirthDateOrTaxNumberProperties
import com.adyen.checkout.card.internal.ui.properties.KCPCardPasswordProperties
import com.adyen.checkout.card.internal.ui.properties.SocialSecurityNumberProperties
import com.adyen.checkout.core.common.helper.CardExpiryDateValidationResult
import com.adyen.checkout.core.common.helper.CardExpiryDateValidator
import com.adyen.checkout.core.common.helper.CardNumberValidationResult
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.helper.CardSecurityCodeValidationResult
import com.adyen.checkout.core.common.helper.CardSecurityCodeValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.isNotRequiredAndValid

internal object CardValidationUtils {

    /**
     * Validate card number.
     */
    internal fun validateCardNumber(
        cardNumber: TextInputComponentState,
        enableLuhnCheck: Boolean,
        isBrandSupported: Boolean
    ): CardNumberValidation {
        if (cardNumber.isNotRequiredAndValid()) return CardNumberValidation.VALID
        val validation = CardNumberValidator.validateCardNumber(cardNumber.text, enableLuhnCheck)
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
        expiryDate: TextInputComponentState,
        fieldPolicy: Brand.FieldPolicy?,
    ): CardExpiryDateValidation {
        if (expiryDate.isNotRequiredAndValid()) return CardExpiryDateValidation.VALID
        val (expiryMonth, expiryYear) = ExpiryDateParser.parseToMonthAndYear(expiryDate.text, returnFullYear = false)
            ?: return CardExpiryDateValidation.INVALID_OTHER_REASON
        val result = CardExpiryDateValidator.validateExpiryDate(expiryMonth = expiryMonth, expiryYear = expiryYear)
        return validateExpiryDate(expiryDate.text, result, fieldPolicy)
    }

    @VisibleForTesting
    internal fun validateExpiryDate(
        expiryDate: String,
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
                        if (expiryDate.isBlank() && fieldPolicy?.isRequired() == false) {
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
     * Validate Holder Name.
     */
    internal fun validateHolderName(
        holderName: TextInputComponentState,
    ): CardHolderNameValidation {
        if (holderName.isNotRequiredAndValid()) return CardHolderNameValidation.VALID
        return if (holderName.text.isBlank()) {
            CardHolderNameValidation.INVALID_BLANK
        } else {
            CardHolderNameValidation.VALID
        }
    }

    /**
     * Validate Security Code.
     */
    internal fun validateSecurityCode(
        securityCode: TextInputComponentState,
        detectedCardType: DetectedCardType?,
    ): CardSecurityCodeValidation {
        if (securityCode.isNotRequiredAndValid()) return CardSecurityCodeValidation.VALID
        val result = CardSecurityCodeValidator.validateSecurityCode(securityCode.text, detectedCardType?.cardBrand)
        return when (result) {
            is CardSecurityCodeValidationResult.Invalid -> CardSecurityCodeValidation.INVALID
            is CardSecurityCodeValidationResult.Valid -> CardSecurityCodeValidation.VALID
        }
    }

    /**
     * Validate Social Security Number.
     */
    internal fun validateSocialSecurityNumber(
        socialSecurityNumber: TextInputComponentState,
    ): CardSocialSecurityNumberValidation {
        if (socialSecurityNumber.isNotRequiredAndValid()) return CardSocialSecurityNumberValidation.VALID

        if (socialSecurityNumber.text.any { !it.isDigit() }) {
            return CardSocialSecurityNumberValidation.INVALID
        }
        return when (socialSecurityNumber.text.length) {
            SocialSecurityNumberProperties.CPF_VALID_LENGTH -> CardSocialSecurityNumberValidation.VALID
            SocialSecurityNumberProperties.CNPJ_VALID_LENGTH -> CardSocialSecurityNumberValidation.VALID
            else -> CardSocialSecurityNumberValidation.INVALID
        }
    }

    /**
     * Validate KCP Birth Date Or Tax Number
     */
    internal fun validateKCPBirthDateOrTaxNumber(
        kcpBirthDateOrTaxNumber: TextInputComponentState,
    ): KCPBirthDateOrTaxNumberValidation {
        if (kcpBirthDateOrTaxNumber.isNotRequiredAndValid()) return KCPBirthDateOrTaxNumberValidation.VALID

        if (kcpBirthDateOrTaxNumber.text.any { !it.isDigit() }) {
            return KCPBirthDateOrTaxNumberValidation.INVALID
        }
        return when (kcpBirthDateOrTaxNumber.text.length) {
            KCPBirthDateOrTaxNumberProperties.KCP_BIRTH_DATE_VALID_LENGTH -> KCPBirthDateOrTaxNumberValidation.VALID
            KCPBirthDateOrTaxNumberProperties.KCP_TAX_NUMBER_VALID_LENGTH -> KCPBirthDateOrTaxNumberValidation.VALID
            else -> KCPBirthDateOrTaxNumberValidation.INVALID
        }
    }

    /**
     * Validate KCP card password
     */
    internal fun validateKCPCardPassword(kcpCardPassword: TextInputComponentState): KCPCardPasswordValidation {
        if (kcpCardPassword.isNotRequiredAndValid()) return KCPCardPasswordValidation.VALID

        if (kcpCardPassword.text.any { !it.isDigit() }) {
            return KCPCardPasswordValidation.INVALID
        }
        return when (kcpCardPassword.text.length) {
            KCPCardPasswordProperties.KCP_CARD_PASSWORD_MAX_LENGTH -> KCPCardPasswordValidation.VALID
            else -> KCPCardPasswordValidation.INVALID
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
    INVALID,
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardHolderNameValidation {
    VALID,
    INVALID_BLANK,
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardSocialSecurityNumberValidation {
    VALID,
    INVALID,
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class KCPBirthDateOrTaxNumberValidation {
    VALID,
    INVALID,
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class KCPCardPasswordValidation {
    VALID,
    INVALID,
}

