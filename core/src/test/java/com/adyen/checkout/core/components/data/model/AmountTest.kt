/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.components.internal.validate
import com.adyen.checkout.core.error.CheckoutError
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class AmountTest {

    @ParameterizedTest
    @MethodSource("validCurrenciesSource")
    fun `when currency is valid then validate returns null`(currency: String) {
        val amount = Amount(currency = currency, value = 1000L)

        val result = amount.validate()

        assertNull(result)
    }

    @ParameterizedTest
    @MethodSource("invalidCurrenciesSource")
    fun `when currency is invalid then validate returns error`(currency: String) {
        val amount = Amount(currency = currency, value = 1000L)

        val result = amount.validate()

        assertNotNull(result)
        assertEquals(CheckoutError.ErrorCode.INVALID_CURRENCY_CODE, result?.code)
    }

    @Test
    fun `when deserializing with missing currency then JSONException is thrown`() {
        val json = JSONObject().apply {
            put("value", 1000L)
        }

        assertThrows(JSONException::class.java) {
            Amount.SERIALIZER.deserialize(json)
        }
    }

    @Test
    fun `when deserializing with invalid currency then validate returns error`() {
        val json = JSONObject().apply {
            put("currency", "INVALID")
            put("value", 1000L)
        }

        val amount = Amount.SERIALIZER.deserialize(json)
        val result = amount.validate()

        assertNotNull(result)
        assertEquals(CheckoutError.ErrorCode.INVALID_CURRENCY_CODE, result?.code)
    }

    @Test
    fun `when deserializing with valid currency then Amount is created`() {
        val json = JSONObject().apply {
            put("currency", "EUR")
            put("value", 1000L)
        }

        val amount = Amount.SERIALIZER.deserialize(json)

        assertEquals("EUR", amount.currency)
        assertEquals(1000L, amount.value)
    }

    @Test
    fun `when serializing Amount then JSON contains currency and value`() {
        val amount = Amount(currency = "USD", value = 2500L)

        val json = Amount.SERIALIZER.serialize(amount)

        assertEquals("USD", json.getString("currency"))
        assertEquals(2500L, json.getLong("value"))
    }

    @Test
    fun `when value is negative then validate returns error`() {
        val amount = Amount(currency = "EUR", value = -1L)

        val result = amount.validate()

        assertNotNull(result)
        assertEquals(CheckoutError.ErrorCode.INVALID_AMOUNT_VALUE, result?.code)
    }

    @Test
    fun `when value is zero then validate returns null`() {
        val amount = Amount(currency = "EUR", value = 0L)

        val result = amount.validate()

        assertNull(result)
    }

    @Test
    fun `when deserializing with negative value then validate returns error`() {
        val json = JSONObject().apply {
            put("currency", "EUR")
            put("value", -100L)
        }

        val amount = Amount.SERIALIZER.deserialize(json)
        val result = amount.validate()

        assertNotNull(result)
        assertEquals(CheckoutError.ErrorCode.INVALID_AMOUNT_VALUE, result?.code)
    }

    @Nested
    inner class FormatTest {

        @Test
        fun `when formatting USD amount then symbol and two decimals are shown`() {
            val amount = Amount(currency = "USD", value = 1337L)

            val result = amount.format(Locale.US)

            assertEquals("$13.37", result)
        }

        @Test
        fun `when formatting EUR amount in US locale then euro symbol and two decimals are shown`() {
            val amount = Amount(currency = "EUR", value = 1337L)

            val result = amount.format(Locale.US)

            assertEquals("\u20AC13.37", result)
        }

        @Test
        fun `when amount value is zero then formatted amount shows zero`() {
            val amount = Amount(currency = "USD", value = 0L)

            val result = amount.format(Locale.US)

            assertEquals("$0.00", result)
        }

        @Test
        fun `when amount is large then grouping separators are applied`() {
            val amount = Amount(currency = "USD", value = 1234567L)

            val result = amount.format(Locale.US)

            assertEquals("$12,345.67", result)
        }

        @Test
        fun `when currency has zero fraction digits then no decimals are shown`() {
            val amount = Amount(currency = "JPY", value = 1337L)

            val result = amount.format(Locale.US)

            assertTrue(result.contains("1,337"), "Expected '1,337' in '$result'")
            assertFalse(result.contains("."), "Expected no decimal separator in '$result'")
        }

        @Test
        fun `when currency has three fraction digits then three decimals are shown`() {
            val amount = Amount(currency = "KWD", value = 1337L)

            val result = amount.format(Locale.US)

            assertTrue(result.contains("1.337"), "Expected '1.337' in '$result'")
        }

        @Test
        fun `when Adyen fraction digits differ from platform default then Adyen value is used`() {
            // ISK defaults to 0 fraction digits on the platform, but Adyen defines 2
            val amount = Amount(currency = "ISK", value = 1337L)

            val result = amount.format(Locale.US)

            assertTrue(result.contains("13.37"), "Expected '13.37' in '$result'")
        }

        @Test
        fun `when formatting an amount with invalid currency then empty string is returned`() {
            val amount = Amount(currency = "TEST", value = 1337L)

            val result = amount.format(Locale.US)

            assertEquals("", result)
        }
    }

    companion object {

        @JvmStatic
        fun validCurrenciesSource() = listOf(
            arguments("EUR"),
            arguments("USD"),
            arguments("GBP"),
            arguments("JPY"),
            arguments("CHF"),
            arguments("AUD"),
            arguments("CAD"),
            arguments("CNY"),
            arguments("BRL"),
            arguments("INR"),
        )

        @JvmStatic
        fun invalidCurrenciesSource() = listOf(
            arguments(""),
            arguments("INVALID"),
            arguments("XYZ"),
            arguments("123"),
            arguments("eur"),
            arguments("Eur"),
            arguments("EU"),
            arguments("EURO"),
        )
    }
}
