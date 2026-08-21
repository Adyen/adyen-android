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
    fun `when fromLanguageTagOrNull is called with valid tag then return locale`() {
        val result = LocaleUtil.fromLanguageTagOrNull("en-US")

        assertEquals(Locale.US, result)
    }

    @ParameterizedTest
    @MethodSource("unparsableLanguageTagsSource")
    fun `when fromLanguageTagOrNull is called with an unparsable tag then return null`(tag: String) {
        val result = LocaleUtil.fromLanguageTagOrNull(tag)

        assertNull(result)
    }

    // Locale.forLanguageTag recovers a usable language from these, so they are not treated as failures.
    @ParameterizedTest
    @MethodSource("leniantlyParsedLanguageTagsSource")
    fun `when fromLanguageTagOrNull is called with a partially valid tag then return locale`(
        tag: String,
        expected: Locale,
    ) {
        val result = LocaleUtil.fromLanguageTagOrNull(tag)

        assertEquals(expected, result)
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

        @JvmStatic
        fun unparsableLanguageTagsSource() = listOf(
            arguments(""),
            // Underscores instead of hyphens, a common mistake when a Java Locale is stringified
            arguments("en_US"),
            arguments("not a language tag"),
            arguments("-en"),
            // Language subtag longer than the 8 characters BCP 47 allows
            arguments("abcdefghi"),
        )

        @JvmStatic
        fun leniantlyParsedLanguageTagsSource() = listOf(
            arguments("en-US", Locale.US),
            arguments("en-", Locale("en")),
            arguments("en--US", Locale("en")),
            arguments("en-US-u-", Locale.US),
        )
    }
}
