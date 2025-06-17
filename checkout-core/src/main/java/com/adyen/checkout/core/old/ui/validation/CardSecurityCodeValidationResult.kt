/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/10/2024.
 */

package com.adyen.checkout.core.old.ui.validation

/**
 * Possible validation results for security code validation. (@see [CardSecurityCodeValidator.validateSecurityCode]
 */
sealed interface CardSecurityCodeValidationResult {
    class Valid : CardSecurityCodeValidationResult
    class Invalid : CardSecurityCodeValidationResult
}
