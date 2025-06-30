/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.old.ui.validation.CardExpiryDateValidator
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberValidationResult
import com.adyen.checkout.giftcard.internal.util.GiftCardPinUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardPinValidationResult
import com.adyen.checkout.mealvoucherfr.R

internal object MealVoucherFRValidationUtils {

    fun validateNumber(number: String): FieldState<String> {
        val validation = GiftCardNumberUtils.validateInputField(number)

        return when (validation) {
            GiftCardNumberValidationResult.VALID -> FieldState(number, Validation.Valid)
            GiftCardNumberValidationResult.INVALID -> FieldState(
                number,
                Validation.Invalid(R.string.checkout_meal_voucher_fr_number_not_valid),
            )
        }
    }

    fun validatePin(pin: String): FieldState<String> {
        val validation = GiftCardPinUtils.validateInputField(pin)

        return when (validation) {
            GiftCardPinValidationResult.VALID -> FieldState(pin, Validation.Valid)
            GiftCardPinValidationResult.INVALID -> FieldState(
                pin,
                Validation.Invalid(R.string.checkout_meal_voucher_fr_pin_not_valid),
            )
        }
    }

    fun validateExpiryDate(expiryDate: String): FieldState<String> {
        return when (val result = CardExpiryDateValidator.validateExpiryDate(expiryDate)) {
            is CardExpiryDateValidationResult.Valid -> FieldState(expiryDate, Validation.Valid)
            is CardExpiryDateValidationResult.Invalid -> {
                when (result) {
                    is CardExpiryDateValidationResult.Invalid.TooFarInTheFuture -> FieldState(
                        expiryDate,
                        Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid_too_far_in_future),
                    )

                    is CardExpiryDateValidationResult.Invalid.TooOld -> FieldState(
                        expiryDate,
                        Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid_too_old),
                    )

                    is CardExpiryDateValidationResult.Invalid.NonParseableDate -> FieldState(
                        expiryDate,
                        Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid),
                    )

                    else -> {
                        // should not happen, due to CardExpiryDateValidationResult being an abstract class
                        FieldState(
                            expiryDate,
                            Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid),
                        )
                    }
                }
            }
        }
    }
}
