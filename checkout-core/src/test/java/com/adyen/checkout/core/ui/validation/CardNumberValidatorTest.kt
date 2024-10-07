/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.ui.validation

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
        assertEquals(expectedValidationResult, actualResult)
    }

    companion object {
        @JvmStatic
        fun cardNumberValidationSource() = listOf(
            arguments(
                "5454545454545454",
                true,
                CardNumberValidationResult.VALID,
            ),
            arguments(
                "3700 0000 0000 002",
                true,
                CardNumberValidationResult.VALID,
            ),
            arguments(
                "55 770 0005 57 7  00 04",
                true,
                CardNumberValidationResult.VALID,
            ),
            arguments(
                "2137f7834a2390",
                true,
                CardNumberValidationResult.INVALID_ILLEGAL_CHARACTERS,
            ),
            arguments(
                "287,7482-3674",
                true,
                CardNumberValidationResult.INVALID_ILLEGAL_CHARACTERS,
            ),
            arguments(
                "1234123",
                true,
                CardNumberValidationResult.INVALID_TOO_SHORT,
            ),
            arguments(
                "37467643756457884754",
                true,
                CardNumberValidationResult.INVALID_TOO_LONG,
            ),
            // Luhn check fails
            arguments(
                "8475 1789 7235 6236",
                true,
                CardNumberValidationResult.INVALID_LUHN_CHECK,
            ),
            // Luhn check is failing but disabled, result is valid
            arguments(
                "192382023091310912",
                false,
                CardNumberValidationResult.VALID,
            ),
        )
    }
}
