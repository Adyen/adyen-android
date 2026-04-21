/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/10/2024.
 */

package com.adyen.checkout.core.old.ui.validation

/**
 * Possible validation results for card number validation. (@see [CardNumberValidator.validateCardNumber]
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
sealed interface CardNumberValidationResult {

    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    class Valid : CardNumberValidationResult

    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    interface Invalid : CardNumberValidationResult {

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class IllegalCharacters : Invalid

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class TooLong : Invalid

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class TooShort : Invalid

        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        class LuhnCheck : Invalid
    }
}
