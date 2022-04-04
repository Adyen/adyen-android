/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard.util

import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.giftcard.R

object GiftCardPinUtils {

    private const val MINIMUM_GIFT_CARD_PIN_LENGTH = 3
    const val MAXIMUM_GIFT_CARD_PIN_LENGTH = 10

    fun validateInputField(giftCardPin: String): FieldState<String> {
        val validation = when {
            giftCardPin.length < MINIMUM_GIFT_CARD_PIN_LENGTH ->
                Validation.Invalid(R.string.checkout_giftcard_pin_not_valid)
            giftCardPin.length > MAXIMUM_GIFT_CARD_PIN_LENGTH ->
                Validation.Invalid(R.string.checkout_giftcard_pin_not_valid)
            else -> Validation.Valid
        }
        return FieldState(giftCardPin, validation)
    }
}
