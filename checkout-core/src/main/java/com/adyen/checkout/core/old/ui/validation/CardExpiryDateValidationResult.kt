/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.core.old.ui.validation

/**
 * Possible validation results for expiry date validation. (@see [CardExpiryDateValidator.validateExpiryDate]
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
sealed interface CardExpiryDateValidationResult {

    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    class Valid : CardExpiryDateValidationResult

    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    interface Invalid : CardExpiryDateValidationResult {

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class TooFarInTheFuture : Invalid

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class TooOld : Invalid

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class NonParseableDate : Invalid
    }
}
