/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.core.ui.validation

/**
 * Possible validation results for expiry date validation. (@see [CardExpiryDateValidator.validateExpiryDate]
 */
sealed interface CardExpiryDateValidationResult {
    class Valid : CardExpiryDateValidationResult
    interface Invalid : CardExpiryDateValidationResult {
        class TooFarInTheFuture : Invalid
        class TooOld : Invalid
        class DateFormat : Invalid
        class OtherReason : Invalid
    }
}
