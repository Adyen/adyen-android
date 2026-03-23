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
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.core.common.helper.CardExpiryDateValidationResult
import com.adyen.checkout.core.common.helper.CardExpiryDateValidator
import com.adyen.checkout.core.common.helper.CardNumberValidationResult
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.helper.CardSecurityCodeValidationResult
import com.adyen.checkout.core.common.helper.CardSecurityCodeValidator
import com.adyen.checkout.core.common.internal.helper.StringUtil
import com.adyen.checkout.core.common.internal.properties.KCPBirthDateOrTaxNumberProperties
import com.adyen.checkout.core.common.internal.properties.KCPBirthDateOrTaxNumberProperties.KCP_BIRTH_DATE_FORMAT
import com.adyen.checkout.core.common.internal.properties.KCPCardPasswordProperties
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal object CardValidationUtils {

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
        expiryDate: String,
        fieldPolicy: Brand.FieldPolicy?,
    ): CardExpiryDateValidation {
        val (expiryMonth, expiryYear) = ExpiryDateParser.parseToMonthAndYear(expiryDate, returnFullYear = false)
            ?: return CardExpiryDateValidation.INVALID_OTHER_REASON
        val result = CardExpiryDateValidator.validateExpiryDate(expiryMonth = expiryMonth, expiryYear = expiryYear)
        return validateExpiryDate(expiryDate, result, fieldPolicy)
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
        holderName: String,
        isRequired: Boolean
    ): CardHolderNameValidation {
        return if (isRequired && holderName.isBlank()) {
            CardHolderNameValidation.INVALID_BLANK
        } else {
            CardHolderNameValidation.VALID
        }
    }

    /**
     * Validate Security Code.
     */
    internal fun validateSecurityCode(
        securityCode: String,
        detectedCardType: DetectedCardType?,
        uiState: RequirementPolicy,
    ): CardSecurityCodeValidation {
        val result = CardSecurityCodeValidator.validateSecurityCode(securityCode, detectedCardType?.cardBrand)
        return validateSecurityCode(securityCode, uiState, result)
    }

    @VisibleForTesting
    internal fun validateSecurityCode(
        securityCode: String,
        uiState: RequirementPolicy,
        validationResult: CardSecurityCodeValidationResult,
    ): CardSecurityCodeValidation {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length

        return when {
            uiState is RequirementPolicy.Hidden -> CardSecurityCodeValidation.VALID_HIDDEN
            uiState is RequirementPolicy.Optional && length == 0 -> CardSecurityCodeValidation.VALID_OPTIONAL_EMPTY
            else -> {
                when (validationResult) {
                    is CardSecurityCodeValidationResult.Invalid -> CardSecurityCodeValidation.INVALID
                    is CardSecurityCodeValidationResult.Valid -> CardSecurityCodeValidation.VALID
                }
            }
        }
    }

    /**
     * Validate Social Security Number.
     */
    @Suppress("ReturnCount")
    internal fun validateSocialSecurityNumber(
        socialSecurityNumber: String,
        requirementPolicy: RequirementPolicy?
    ): CardSocialSecurityNumberValidation {
        // allow empty value unless field is required
        if (socialSecurityNumber.isEmpty() && requirementPolicy != RequirementPolicy.Required) {
            return CardSocialSecurityNumberValidation.VALID
        }

        if (socialSecurityNumber.any { !it.isDigit() }) {
            return CardSocialSecurityNumberValidation.INVALID
        }
        return when (socialSecurityNumber.length) {
            SocialSecurityNumberProperties.CPF_VALID_LENGTH,
            SocialSecurityNumberProperties.CNPJ_VALID_LENGTH -> CardSocialSecurityNumberValidation.VALID

            else -> CardSocialSecurityNumberValidation.INVALID
        }
    }

    /**
     * Validate KCP Birth Date Or Tax Number
     */
    @Suppress("ReturnCount")
    internal fun validateKCPBirthDateOrTaxNumber(
        kcpBirthDateOrTaxNumber: String,
        requirementPolicy: RequirementPolicy?
    ): KCPBirthDateOrTaxNumberValidation {
        // allow empty value unless field is required
        if (kcpBirthDateOrTaxNumber.isEmpty() && requirementPolicy != RequirementPolicy.Required) {
            return KCPBirthDateOrTaxNumberValidation.VALID
        }
        if (kcpBirthDateOrTaxNumber.any { !it.isDigit() }) {
            return KCPBirthDateOrTaxNumberValidation.INVALID
        }
        return when (kcpBirthDateOrTaxNumber.length) {
            KCPBirthDateOrTaxNumberProperties.KCP_TAX_NUMBER_VALID_LENGTH -> KCPBirthDateOrTaxNumberValidation.VALID
            KCPBirthDateOrTaxNumberProperties.KCP_BIRTH_DATE_VALID_LENGTH -> {
                val matchesDateFormat = DateUtils.matchesFormat(kcpBirthDateOrTaxNumber, KCP_BIRTH_DATE_FORMAT)
                if (matchesDateFormat) {
                    KCPBirthDateOrTaxNumberValidation.VALID
                } else {
                    KCPBirthDateOrTaxNumberValidation.INVALID
                }
            }

            else -> KCPBirthDateOrTaxNumberValidation.INVALID
        }
    }

    /**
     * Validate KCP card password
     */
    @Suppress("ReturnCount")
    internal fun validateKCPCardPassword(
        kcpCardPassword: String,
        requirementPolicy: RequirementPolicy?
    ): KCPCardPasswordValidation {
        // allow empty value unless field is required
        if (kcpCardPassword.isEmpty() && requirementPolicy != RequirementPolicy.Required) {
            return KCPCardPasswordValidation.VALID
        }
        if (kcpCardPassword.any { !it.isDigit() }) {
            return KCPCardPasswordValidation.INVALID
        }
        return when (kcpCardPassword.length) {
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
    VALID_HIDDEN,
    VALID_OPTIONAL_EMPTY,
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
