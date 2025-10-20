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
import com.adyen.checkout.core.common.helper.CardNumberValidationResult
import com.adyen.checkout.core.common.helper.CardNumberValidator

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
