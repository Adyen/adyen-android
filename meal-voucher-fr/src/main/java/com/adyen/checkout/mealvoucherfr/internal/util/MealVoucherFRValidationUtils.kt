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
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidator
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberValidationResult
import com.adyen.checkout.giftcard.internal.util.GiftCardPinUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardPinValidationResult
import com.adyen.checkout.mealvoucherfr.R
import org.jetbrains.annotations.VisibleForTesting
import java.util.Calendar
import java.util.GregorianCalendar

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

    fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return validateExpiryDate(expiryDate, GregorianCalendar.getInstance())
    }

    @VisibleForTesting
    internal fun validateExpiryDate(expiryDate: ExpiryDate, calendar: Calendar): FieldState<ExpiryDate> {
        return when (CardExpiryDateValidator.validateExpiryDate(expiryDate, calendar)) {
            CardExpiryDateValidationResult.VALID -> FieldState(expiryDate, Validation.Valid)
            CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid_too_far_in_future),
            )

            CardExpiryDateValidationResult.INVALID_TOO_OLD -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid_too_old),
            )

            CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
            CardExpiryDateValidationResult.INVALID_OTHER_REASON -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_fr_expiry_date_not_valid),
            )
        }
    }
}
