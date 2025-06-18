/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.old.ui.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class CardNumberValidatorTest {

    @ParameterizedTest
    @MethodSource("cardNumberValidationSource")
    fun `when validateCardNumber is called, then expected validation result is returned`(
        number: String,
        enableLuhnCheck: Boolean,
        expectedValidationResult: CardNumberValidationResult,
    ) {
        val actualResult = CardNumberValidator.validateCardNumber(number, enableLuhnCheck)
        assertEquals(expectedValidationResult.javaClass, actualResult.javaClass)
    }

    companion object {
        @JvmStatic
        fun cardNumberValidationSource() = listOf(
            arguments(
                "5454545454545454",
                true,
                CardNumberValidationResult.Valid(),
            ),
            arguments(
                "3700 0000 0000 002",
                true,
                CardNumberValidationResult.Valid(),
            ),
            arguments(
                "55 770 0005 57 7  00 04",
                true,
                CardNumberValidationResult.Valid(),
            ),
            arguments(
                "2137f7834a2390",
                true,
                CardNumberValidationResult.Invalid.IllegalCharacters(),
            ),
            arguments(
                "287,7482-3674",
                true,
                CardNumberValidationResult.Invalid.IllegalCharacters(),
            ),
            arguments(
                "1234123",
                true,
                CardNumberValidationResult.Invalid.TooShort(),
            ),
            arguments(
                "37467643756457884754",
                true,
                CardNumberValidationResult.Invalid.TooLong(),
            ),
            // Luhn check fails
            arguments(
                "8475 1789 7235 6236",
                true,
                CardNumberValidationResult.Invalid.LuhnCheck(),
            ),
            // Luhn check is failing but disabled, result is valid
            arguments(
                "192382023091310912",
                false,
                CardNumberValidationResult.Valid(),
            ),
        )
    }
}
