/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/10/2024.
 */

package com.adyen.checkout.core.common.helper

/**
 * Possible validation results for card number validation. (@see [CardNumberValidator.validateCardNumber]
 */
sealed interface CardNumberValidationResult {
    class Valid : CardNumberValidationResult
    interface Invalid : CardNumberValidationResult {
        class IllegalCharacters : Invalid
        class TooLong : Invalid
        class TooShort : Invalid
        class LuhnCheck : Invalid
    }
}
