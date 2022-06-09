package com.adyen.checkout.giftcard

import com.adyen.checkout.giftcard.util.GiftCardNumberUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GiftCardNumberUtilsTest {

    @Test
    fun formatEmptyInput() {
        assertEquals("", GiftCardNumberUtils.formatInput(""))
    }

    @Test
    fun format3DigitInput() {
        assertEquals("123", GiftCardNumberUtils.formatInput("123"))
    }

    @Test
    fun format4DigitInput() {
        assertEquals("1234", GiftCardNumberUtils.formatInput("1234"))
    }

    @Test
    fun format5DigitInput() {
        assertEquals("1234 5", GiftCardNumberUtils.formatInput("12345"))
    }

    @Test
    fun format7DigitInput() {
        assertEquals("1234 567", GiftCardNumberUtils.formatInput("1234567"))
    }

    @Test
    fun format8DigitInput() {
        assertEquals("1234 5678", GiftCardNumberUtils.formatInput("12345678"))
    }

    @Test
    fun format9DigitInput() {
        assertEquals("1234 5678 9", GiftCardNumberUtils.formatInput("123456789"))
    }

    @Test
    fun format16DigitInput() {
        assertEquals("1234 5678 4321 9876", GiftCardNumberUtils.formatInput("1234567843219876"))
    }

    @Test
    fun format19DigitInput() {
        assertEquals("1234 5678 4321 9876 000", GiftCardNumberUtils.formatInput("1234567843219876000"))
    }

    @Test
    fun formatAlphanumericInputWithSpaces() {
        assertEquals("hioj rfg1 247f dsgd 8453 h3h", GiftCardNumberUtils.formatInput("hioj rfg1247fds gd8453 h3h"))
    }
}
