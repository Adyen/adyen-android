/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AmountTest {

    @ParameterizedTest
    @MethodSource("validCurrenciesSource")
    fun `when currency is valid then Amount is created successfully`(currency: String) {
        val amount = Amount(currency = currency, value = 1000L)

        assertEquals(currency, amount.currency)
        assertEquals(1000L, amount.value)
    }

    @ParameterizedTest
    @MethodSource("invalidCurrenciesSource")
    fun `when currency is invalid then CheckoutException is thrown`(currency: String) {
        val exception = assertThrows(CheckoutException::class.java) {
            Amount(currency = currency, value = 1000L)
        }

        assertEquals(CheckoutError.ErrorCode.INVALID_CURRENCY_CODE, exception.error.code)
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
    fun `when deserializing with invalid currency then CheckoutException is thrown`() {
        val json = JSONObject().apply {
            put("currency", "INVALID")
            put("value", 1000L)
        }

        val exception = assertThrows(CheckoutException::class.java) {
            Amount.SERIALIZER.deserialize(json)
        }

        assertEquals(CheckoutError.ErrorCode.INVALID_CURRENCY_CODE, exception.error.code)
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
