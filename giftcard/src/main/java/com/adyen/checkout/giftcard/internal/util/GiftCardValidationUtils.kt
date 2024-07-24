/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2024.
 */

package com.adyen.checkout.giftcard.internal.util

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.giftcard.R
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate

internal object GiftCardValidationUtils {

    fun validateNumber(number: String): FieldState<String> {
        val validation = GiftCardNumberUtils.validateInputField(number)

        return when (validation) {
            GiftCardNumberValidationResult.VALID -> FieldState(number, Validation.Valid)
            GiftCardNumberValidationResult.INVALID -> FieldState(
                number,
                Validation.Invalid(R.string.checkout_giftcard_number_not_valid),
            )
        }
    }

    fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return FieldState(expiryDate, Validation.Valid)
    }
}