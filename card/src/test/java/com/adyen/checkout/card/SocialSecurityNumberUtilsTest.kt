package com.adyen.checkout.card

import com.adyen.checkout.card.util.SocialSecurityNumberUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SocialSecurityNumberUtilsTest {

    @Test
    fun testFormatInputWithEmptyInput() {
        assertEquals("", SocialSecurityNumberUtils.formatInput(""))
    }

    @Test
    fun testFormatInputWithPartialCPFInputAfterRemovingDigit() {
        assertEquals("111", SocialSecurityNumberUtils.formatInput("111."))
    }

    @Test
    fun testFormatInputWithPartialCPFInputAfterInsertingDigit() {
        assertEquals("123.1", SocialSecurityNumberUtils.formatInput("1231"))
    }

    @Test
    fun testFormatInputWithCompleteCPFInput() {
        assertEquals("123.123.123-12", SocialSecurityNumberUtils.formatInput("12312312312"))
    }

    @Test
    fun testFormatInputWithFormattedCPFInput() {
        assertEquals("123.123.123-12", SocialSecurityNumberUtils.formatInput("123.123.123-12"))
    }

    @Test
    fun testFormatInputWithCPFtoCNPJ() {
        assertEquals("11.111.111/1111", SocialSecurityNumberUtils.formatInput("111.111.111-111"))
    }

    @Test
    fun testFormatInputWithPartialCNPJInput() {
        assertEquals("12.123.123/1234", SocialSecurityNumberUtils.formatInput("121231231234"))
    }

    @Test
    fun testFormatInputWithPartialCNPJInputAfterInsertingDigit() {
        assertEquals("12.123.123/1234-1", SocialSecurityNumberUtils.formatInput("12.123.123/12341"))
    }

    @Test
    fun testFormatInputWithPartialCNPJInputAfterRemovingDigit() {
        assertEquals("12.123.123/1234", SocialSecurityNumberUtils.formatInput("12.123.123/1234-"))
    }

    @Test
    fun testFormatInputWithCNPJtoCPF() {
        assertEquals("121.231.231-23", SocialSecurityNumberUtils.formatInput("12.123.123/123"))
    }

    @Test
    fun testFormatInputWithCompleteCNPJInput() {
        assertEquals("12.123.123/1234-12", SocialSecurityNumberUtils.formatInput("12123123123412"))
    }

    @Test
    fun testFormatInputWithFormattedCNPJInput() {
        assertEquals("12.123.123/1234", SocialSecurityNumberUtils.formatInput("12.123.123/1234"))
    }
}
