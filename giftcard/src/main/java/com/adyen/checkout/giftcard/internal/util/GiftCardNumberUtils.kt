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
object GiftCardNumberUtils {

    private const val CARD_NUMBER_MASK_GROUP_LENGTH = 4
    const val DIGIT_SEPARATOR = ' '
    private const val MINIMUM_GIFT_CARD_NUMBER_LENGTH = 15
    const val MAXIMUM_GIFT_CARD_NUMBER_LENGTH = 32
    const val MAX_DIGIT_SEPARATOR_COUNT =
        (MAXIMUM_GIFT_CARD_NUMBER_LENGTH / CARD_NUMBER_MASK_GROUP_LENGTH) - 1 // 32 digits, grouped by 4 -> 7 spaces

    fun formatInput(inputString: String): String {
        var rawInput = getRawValue(inputString)
        val resultBuilder = StringBuilder()
        while (rawInput.isNotEmpty()) {
            val part = rawInput.take(CARD_NUMBER_MASK_GROUP_LENGTH)
            if (resultBuilder.isNotEmpty()) {
                resultBuilder.append(DIGIT_SEPARATOR)
            }
            resultBuilder.append(part)
            rawInput = rawInput.removePrefix(part)
        }
        return resultBuilder.toString()
    }

    fun getRawValue(text: String): String {
        return text.replace(DIGIT_SEPARATOR.toString(), "")
    }

    fun validateInputField(giftCardNumber: String): GiftCardNumberValidationResult {
        val rawInput = getRawValue(giftCardNumber)

        return when {
            rawInput.length < MINIMUM_GIFT_CARD_NUMBER_LENGTH -> GiftCardNumberValidationResult.INVALID
            rawInput.length > MAXIMUM_GIFT_CARD_NUMBER_LENGTH -> GiftCardNumberValidationResult.INVALID
            else -> GiftCardNumberValidationResult.VALID
        }
    }
}
