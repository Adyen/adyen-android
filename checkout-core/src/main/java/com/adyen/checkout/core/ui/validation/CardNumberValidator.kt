/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.ui.validation

import com.adyen.checkout.core.internal.util.StringUtil

object CardNumberValidator {

    // Luhn Check
    private const val RADIX = 10
    private const val FIVE_DIGIT = 5

    // Card Number
    private const val MINIMUM_CARD_NUMBER_LENGTH = 12
    const val MAXIMUM_CARD_NUMBER_LENGTH = 19

    fun validateCardNumber(number: String, enableLuhnCheck: Boolean): CardNumberValidationResult {
        val normalizedNumber = StringUtil.normalize(number)
        val length = normalizedNumber.length
        return when {
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedNumber) ->
                CardNumberValidationResult.INVALID_ILLEGAL_CHARACTERS

            length > MAXIMUM_CARD_NUMBER_LENGTH -> CardNumberValidationResult.INVALID_TOO_LONG
            length < MINIMUM_CARD_NUMBER_LENGTH -> CardNumberValidationResult.INVALID_TOO_SHORT
            enableLuhnCheck && !isLuhnChecksumValid(normalizedNumber) ->
                CardNumberValidationResult.INVALID_LUHN_CHECK

            else -> CardNumberValidationResult.VALID
        }
    }

    @Suppress("MagicNumber")
    private fun isLuhnChecksumValid(normalizedNumber: String): Boolean {
        var s1 = 0
        var s2 = 0
        val reverse = StringBuffer(normalizedNumber).reverse().toString()
        for (i in reverse.indices) {
            val digit = Character.digit(reverse[i], RADIX)
            if (i % 2 == 0) {
                s1 += digit
            } else {
                s2 += 2 * digit
                if (digit >= FIVE_DIGIT) {
                    s2 -= 9
                }
            }
        }
        return (s1 + s2) % 10 == 0
    }
}
