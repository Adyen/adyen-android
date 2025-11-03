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
}
