/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

internal class CardValidationMapper {

    fun mapCardNumberValidation(validation: CardNumberValidation): CheckoutLocalizationKey? {
        return when (validation) {
            CardNumberValidation.VALID -> null
            CardNumberValidation.INVALID_ILLEGAL_CHARACTERS -> CheckoutLocalizationKey.CARD_NUMBER_INVALID
            CardNumberValidation.INVALID_LUHN_CHECK -> CheckoutLocalizationKey.CARD_NUMBER_INVALID
            CardNumberValidation.INVALID_TOO_SHORT -> CheckoutLocalizationKey.CARD_NUMBER_INVALID
            CardNumberValidation.INVALID_TOO_LONG -> CheckoutLocalizationKey.CARD_NUMBER_INVALID
            CardNumberValidation.INVALID_UNSUPPORTED_BRAND ->
                CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND

            CardNumberValidation.INVALID_OTHER_REASON -> CheckoutLocalizationKey.CARD_NUMBER_INVALID
        }
    }

    fun mapExpiryDateValidation(
        validation: CardExpiryDateValidation,
    ): CheckoutLocalizationKey? {
        return when (validation) {
            CardExpiryDateValidation.VALID -> null
            CardExpiryDateValidation.VALID_NOT_REQUIRED -> null
            CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE ->
                CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_FAR_IN_THE_FUTURE

            CardExpiryDateValidation.INVALID_TOO_OLD -> CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID_TOO_OLD
            CardExpiryDateValidation.INVALID_OTHER_REASON -> CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID
        }
    }

    fun mapSecurityCodeValidation(
        validation: CardSecurityCodeValidation
    ): CheckoutLocalizationKey? {
        return when (validation) {
            CardSecurityCodeValidation.VALID -> null
            CardSecurityCodeValidation.VALID_HIDDEN -> null
            CardSecurityCodeValidation.VALID_OPTIONAL_EMPTY -> null
            CardSecurityCodeValidation.INVALID -> CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID
        }
    }
}
