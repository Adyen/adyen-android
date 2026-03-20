/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/3/2026.
 */

package com.adyen.checkout.card.internal.helper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ExpiryDateParserTest {

    @Test
    fun `when input is valid and returnFullYear is true then return month and full year`() {
        val result = ExpiryDateParser.parseToMonthAndYear("0326", true)
        assertEquals("03" to "2026", result)
    }

    @Test
    fun `when input is valid and returnFullYear is false then return month and short year`() {
        val result = ExpiryDateParser.parseToMonthAndYear("0326", false)
        assertEquals("03" to "26", result)
    }

    @Test
    fun `when input length is too short then return null`() {
        val result = ExpiryDateParser.parseToMonthAndYear("032", true)
        assertNull(result)
    }

    @Test
    fun `when input length is too long then return null`() {
        val result = ExpiryDateParser.parseToMonthAndYear("03261", true)
        assertNull(result)
    }

    @Test
    fun `when input is empty then return null`() {
        val result = ExpiryDateParser.parseToMonthAndYear("", true)
        assertNull(result)
    }

    @Test
    fun `when month is double digits then it is parsed correctly`() {
        val result = ExpiryDateParser.parseToMonthAndYear("1212", true)
        assertEquals("12" to "2012", result)
    }
}
