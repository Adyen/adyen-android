package com.adyen.checkout.card.internal.helper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CardBinHelperTest {

    @Test
    fun `when card number is valid and has exactly 16 digits then return first 8 digits`() {
        val result = CardBinHelper.getBin(
            cardNumber = "1234567890123456",
            isValid = true,
        )

        assertEquals("12345678", result)
    }

    @Test
    fun `when card number is valid and has more than 16 digits then return first 8 digits`() {
        val result = CardBinHelper.getBin(
            cardNumber = "1234567890123456789",
            isValid = true,
        )

        assertEquals("12345678", result)
    }

    @Test
    fun `when card number is valid and has less than 16 digits then return first 6 digits`() {
        val result = CardBinHelper.getBin(
            cardNumber = "123456789012345",
            isValid = true,
        )

        assertEquals("123456", result)
    }

    @Test
    fun `when card number is not valid and has at least 16 digits then return first 6 digits`() {
        val result = CardBinHelper.getBin(
            cardNumber = "1234567890123456",
            isValid = false,
        )

        assertEquals("123456", result)
    }

    @Test
    fun `when card number has less than 6 digits then return full card number`() {
        val result = CardBinHelper.getBin(
            cardNumber = "1234",
            isValid = false,
        )

        assertEquals("1234", result)
    }

    @Test
    fun `when card number is empty then return empty string`() {
        val result = CardBinHelper.getBin(
            cardNumber = "",
            isValid = false,
        )

        assertEquals("", result)
    }
}
