package com.adyen.checkout.giftcard.internal.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class GiftCardNumberUtilsTest {

    @ParameterizedTest
    @MethodSource("giftCardNumberFormatSource")
    fun `when formatInput is called, then expected formatted number is returned`(
        giftCardNumber: String,
        expectedFormattedNumber: String,
    ) {
        val actualResult = GiftCardNumberUtils.formatInput(giftCardNumber)

        assertEquals(expectedFormattedNumber, actualResult)
    }

    @ParameterizedTest
    @MethodSource("giftCardNumberRawValueSource")
    fun `when getRawValue is called, then expected raw value is returned`(
        giftCardNumber: String,
        expectedRawValue: String,
    ) {
        val actualResult = GiftCardNumberUtils.getRawValue(giftCardNumber)

        assertEquals(expectedRawValue, actualResult)
    }

    @ParameterizedTest
    @MethodSource("giftCardNumberValidationSource")
    fun `when validateInputField is called, then expected validation result is returned`(
        giftCardNumber: String,
        expectedValidationResult: GiftCardNumberValidationResult
    ) {
        val actualResult = GiftCardNumberUtils.validateInputField(giftCardNumber)

        assertEquals(expectedValidationResult, actualResult)
    }

    companion object {

        @JvmStatic
        fun giftCardNumberFormatSource() = listOf(
            // giftCardNumber, expectedFormattedNumber
            arguments("", ""),
            arguments("123", "123"),
            arguments("1234", "1234"),
            arguments("12345", "1234 5"),
            arguments("1234567", "1234 567"),
            arguments("12345678", "1234 5678"),
            arguments("123456789", "1234 5678 9"),
            arguments("1234567843219876", "1234 5678 4321 9876"),
            arguments("1234567843219876000", "1234 5678 4321 9876 000"),
            arguments("hioj rfg1247fds gd8453 h3h", "hioj rfg1 247f dsgd 8453 h3h"),
        )

        @JvmStatic
        fun giftCardNumberRawValueSource() = listOf(
            // giftCardNumber, expectedRawValue
            arguments("", ""),
            arguments("123", "123"),
            arguments("1234 5", "12345"),
            arguments("1234 5678", "12345678"),
            arguments("1234 5678 9", "123456789"),
            arguments("1234 5678 4321 9876 000", "1234567843219876000"),
            arguments("hioj rfg1247fds gd8453 h3h", "hiojrfg1247fdsgd8453h3h"),
        )

        @JvmStatic
        fun giftCardNumberValidationSource() = listOf(
            // giftCardNumber, expectedValidationResult
            arguments("", GiftCardNumberValidationResult.INVALID),
            arguments("12345678901234", GiftCardNumberValidationResult.INVALID),
            arguments("123456789012345678901234567890123", GiftCardNumberValidationResult.INVALID),
            arguments("123456789012345678901234567890", GiftCardNumberValidationResult.VALID),
        )
    }
}
