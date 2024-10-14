/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/8/2024.
 */

package com.adyen.checkout.giftcard.internal.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class GiftCardPinUtilsTest {

    @ParameterizedTest
    @MethodSource("giftCardPinValidationSource")
    fun `when validateInputField is called, then expected validation result is returned`(
        giftCardPin: String,
        expectedValidationResult: GiftCardPinValidationResult
    ) {
        val actual = GiftCardPinUtils.validateInputField(giftCardPin)

        assertEquals(expectedValidationResult, actual)
    }

    companion object {

        @JvmStatic
        fun giftCardPinValidationSource() = listOf(
            // giftCardPin, expectedValidationResult
            arguments("", GiftCardPinValidationResult.INVALID),
            arguments("12", GiftCardPinValidationResult.INVALID),
            arguments("123", GiftCardPinValidationResult.VALID),
            arguments("1234567890", GiftCardPinValidationResult.VALID),
            arguments("123456789012345678901234567890123", GiftCardPinValidationResult.INVALID),
        )
    }
}
