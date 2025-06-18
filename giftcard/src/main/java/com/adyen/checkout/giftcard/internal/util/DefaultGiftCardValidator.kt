/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.giftcard.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.core.old.ui.model.ExpiryDate

internal class DefaultGiftCardValidator : GiftCardValidator {
    override fun validateNumber(number: String): FieldState<String> {
        return GiftCardValidationUtils.validateNumber(number)
    }

    override fun validatePin(pin: String): FieldState<String> {
        return GiftCardValidationUtils.validatePin(pin)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return GiftCardValidationUtils.validateExpiryDate(expiryDate)
    }
}
