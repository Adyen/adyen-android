/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard.internal.util

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object GiftCardPinUtils {

    private const val MINIMUM_GIFT_CARD_PIN_LENGTH = 3
    private const val MAXIMUM_GIFT_CARD_PIN_LENGTH = 10

    fun validateInputField(giftCardPin: String) = when {
        giftCardPin.length < MINIMUM_GIFT_CARD_PIN_LENGTH -> GiftCardPinValidationResult.INVALID
        giftCardPin.length > MAXIMUM_GIFT_CARD_PIN_LENGTH -> GiftCardPinValidationResult.INVALID
        else -> GiftCardPinValidationResult.VALID
    }
}
