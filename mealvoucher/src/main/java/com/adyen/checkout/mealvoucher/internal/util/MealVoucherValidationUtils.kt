/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberValidationResult
import com.adyen.checkout.mealvoucher.R
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.util.ExpiryDateValidationResult
import com.adyen.checkout.ui.core.internal.util.ExpiryDateValidationUtils
import org.jetbrains.annotations.VisibleForTesting
import java.util.Calendar
import java.util.GregorianCalendar

internal object MealVoucherValidationUtils {

    fun validateNumber(number: String): FieldState<String> {
        val validation = GiftCardNumberUtils.validateInputField(number)

        return when (validation) {
            GiftCardNumberValidationResult.VALID -> FieldState(number, Validation.Valid)
            GiftCardNumberValidationResult.INVALID -> FieldState(
                number,
                Validation.Invalid(R.string.checkout_meal_voucher_number_not_valid),
            )
        }
    }

    fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return validateExpiryDate(expiryDate, GregorianCalendar.getInstance())
    }

    @VisibleForTesting
    internal fun validateExpiryDate(expiryDate: ExpiryDate, calendar: Calendar): FieldState<ExpiryDate> {
        return when (ExpiryDateValidationUtils.validateExpiryDateInternal(expiryDate, calendar)) {
            ExpiryDateValidationResult.VALID -> FieldState(expiryDate, Validation.Valid)
            ExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_expiry_date_not_valid_too_far_in_future),
            )

            ExpiryDateValidationResult.INVALID_TOO_OLD -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_expiry_date_not_valid_too_old),
            )

            ExpiryDateValidationResult.INVALID_EXPIRY_DATE -> FieldState(
                expiryDate,
                Validation.Invalid(R.string.checkout_meal_voucher_expiry_date_not_valid),
            )
        }
    }
}
