/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.giftcard.internal.util.GiftCardValidator
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate

internal class MealVoucherFRValidator : GiftCardValidator {

    override fun validateNumber(number: String): FieldState<String> {
        return MealVoucherFRValidationUtils.validateNumber(number)
    }

    override fun validatePin(pin: String): FieldState<String> {
        return MealVoucherFRValidationUtils.validatePin(pin)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return MealVoucherFRValidationUtils.validateExpiryDate(expiryDate)
    }
}
