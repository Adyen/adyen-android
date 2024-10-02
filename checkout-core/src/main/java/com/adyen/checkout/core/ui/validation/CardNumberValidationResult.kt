/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/10/2024.
 */

package com.adyen.checkout.core.ui.validation

enum class CardNumberValidationResult {
    INVALID_ILLEGAL_CHARACTERS,
    INVALID_TOO_LONG,
    INVALID_TOO_SHORT,
    INVALID_LUHN_CHECK,
    VALID
}
