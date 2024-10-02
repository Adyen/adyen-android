/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.card.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidator
import com.adyen.checkout.core.ui.validation.CardNumberValidationResult
import com.adyen.checkout.core.ui.validation.CardNumberValidator
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidationResult
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidator
import java.util.Calendar
import java.util.GregorianCalendar

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardValidationUtils {

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
        calendar: Calendar = GregorianCalendar.getInstance()
    ): CardExpiryDateValidationResult {
        return CardExpiryDateValidator.validateExpiryDate(expiryDate, calendar)
    }

    /**
     * Validate Security Code.
     */
    internal fun validateSecurityCode(
        securityCode: String,
        detectedCardType: DetectedCardType?
    ): CardSecurityCodeValidationResult {
        return CardSecurityCodeValidator.validateSecurityCode(securityCode, detectedCardType?.cardBrand)
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
