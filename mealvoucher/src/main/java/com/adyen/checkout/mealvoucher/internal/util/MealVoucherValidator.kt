/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardPinUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardValidator
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate

internal class MealVoucherValidator : GiftCardValidator {

    override fun validateNumber(number: String): FieldState<String> {
        return GiftCardNumberUtils.validateInputField(number)
    }

    override fun validatePin(pin: String): FieldState<String> {
        return GiftCardPinUtils.validateInputField(pin)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return MealVoucherValidationUtils.validateExpiryDate(expiryDate)
    }
}
