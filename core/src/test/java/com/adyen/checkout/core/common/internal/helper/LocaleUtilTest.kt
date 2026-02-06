/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.error.CheckoutError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

@Suppress("DEPRECATION")
internal class LocaleUtilTest {

    @ParameterizedTest
    @MethodSource("validLocalesSource")
    fun `when locale is valid then return null`(locale: Locale) {
        val result = LocaleUtil.validateLocale(locale)

        assertNull(result)
    }

    @ParameterizedTest
    @MethodSource("invalidLocalesSource")
    fun `when locale is invalid then return error`(locale: Locale) {
        val result = LocaleUtil.validateLocale(locale)

        assertNotNull(result)
        assertEquals(CheckoutError.ErrorCode.INVALID_LOCALE, result?.code)
    }

    @Test
    fun `when locale is invalid then error contains cause`() {
        val invalidLocale = Locale("español")

        val result = LocaleUtil.validateLocale(invalidLocale)

        assertNotNull(result)
        assertNotNull(result?.cause)
    }

    @Test
    fun `when locale is invalid then error message contains locale`() {
        val invalidLocale = Locale("español")

        val result = LocaleUtil.validateLocale(invalidLocale)

        assertNotNull(result)
        assertTrue(result?.message?.contains("español") == true)
    }

    @Test
    fun `when fromLanguageTag is called with valid tag then return locale`() {
        val result = LocaleUtil.fromLanguageTag("en-US")

        assertEquals(Locale.US, result)
    }

    @Test
    fun `when fromLanguageTag is called with empty tag then return empty locale`() {
        val result = LocaleUtil.fromLanguageTag("")

        assertEquals(Locale(""), result)
    }

    companion object {

        @JvmStatic
        fun validLocalesSource() = listOf(
            arguments(Locale.US),
            arguments(Locale.UK),
            arguments(Locale.GERMANY),
            arguments(Locale.FRANCE),
            arguments(Locale.JAPAN),
            arguments(Locale.CHINA),
            arguments(Locale("en")),
            arguments(Locale("en", "US")),
            arguments(Locale("en", "GB")),
            arguments(Locale("de", "DE")),
            arguments(Locale("nl", "NL")),
            arguments(Locale("pt", "BR")),
            arguments(Locale("zh", "CN")),
            arguments(Locale("zh", "TW")),
        )

        @JvmStatic
        fun invalidLocalesSource() = listOf(
            // Language with non-ASCII characters
            arguments(Locale("español")),
            // Invalid variant
            arguments(Locale("de", "HANS")),
            // Language code too long
            arguments(Locale("toolongcode")),
            // Invalid characters in language
            arguments(Locale("en-US")),
        )
    }
}
