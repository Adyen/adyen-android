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
    fun `when input month is not valid then return null`() {
        val result = ExpiryDateParser.parseToMonthAndYear("4520", true)
        assertNull(result)
    }

    @Test
    fun `when input has illegal characters then return null`() {
        val result = ExpiryDateParser.parseToMonthAndYear("asbg", true)
        assertNull(result)
    }

    @Test
    fun `when month is double digits then it is parsed correctly`() {
        val result = ExpiryDateParser.parseToMonthAndYear("1212", true)
        assertEquals("12" to "2012", result)
    }

    @Test
    fun `when formatting valid month and year then return MMyy string`() {
        val result = ExpiryDateParser.formatToMMyy(3, 2026)
        assertEquals("0326", result)
    }

    @Test
    fun `when formatting single digit month then it is zero padded`() {
        val result = ExpiryDateParser.formatToMMyy(1, 2030)
        assertEquals("0130", result)
    }

    @Test
    fun `when formatting double digit month then it is not padded`() {
        val result = ExpiryDateParser.formatToMMyy(12, 2025)
        assertEquals("1225", result)
    }

    @Test
    fun `when formatting with two digit year then it is used directly`() {
        val result = ExpiryDateParser.formatToMMyy(6, 30)
        assertEquals("0630", result)
    }

    @Test
    fun `when month is null then return empty string`() {
        val result = ExpiryDateParser.formatToMMyy(null, 2026)
        assertEquals("", result)
    }

    @Test
    fun `when year is null then return empty string`() {
        val result = ExpiryDateParser.formatToMMyy(3, null)
        assertEquals("", result)
    }

    @Test
    fun `when both month and year are null then return empty string`() {
        val result = ExpiryDateParser.formatToMMyy(null, null)
        assertEquals("", result)
    }
}
