/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.components.internal.validate
import com.adyen.checkout.core.error.CheckoutError
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

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
    fun `when deserializing with missing currency then ModelSerializationException is thrown`() {
        val json = JSONObject().apply {
            put("value", 1000L)
        }

        assertThrows(ModelSerializationException::class.java) {
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
