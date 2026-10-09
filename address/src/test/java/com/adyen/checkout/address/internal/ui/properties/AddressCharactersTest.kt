/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.properties

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class AddressCharactersTest {

    @Test
    fun `when the value is a singapore unit number then it is allowed`() {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters("#01-23")

        // THEN
        assertFalse(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["123 Main St., Apt #4", "?+_=!@#\$%&()[]"])
    fun `when the value has ordinary punctuation then it is allowed`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertFalse(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Müller Straße", "東京都渋谷区1-2-3", "شارع الملك"])
    fun `when the value is not latin then it is allowed`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertFalse(result)
    }

    /** 🏠 is outside the basic plane, so it is only found when the value is read by code point. */
    @ParameterizedTest
    @ValueSource(strings = ["😀", "🏠", "🎉", "❤️", "Hello 😀 World"])
    fun `when the value has an emoji then it is rejected`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertTrue(result)
    }

    /** © starts the first range, U+1FFFD ends the last one. */
    @ParameterizedTest
    @ValueSource(strings = ["©", "\uD83F\uDFFD"])
    fun `when the value has a pictograph at the edge of the ranges then it is rejected`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertTrue(result)
    }

    /** ☆ (U+2606) sits in the gap between the ranges 2600..2605 and 2607..2612. */
    @Test
    fun `when the value has a symbol between the ranges then it is allowed`() {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters("☆")

        // THEN
        assertFalse(result)
    }

    @Test
    fun `when the value has a flag then it is rejected`() {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters("🇫🇷")

        // THEN
        assertTrue(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1\uFE0F\u20E3", "#\uFE0F\u20E3", "*\u20E3"])
    fun `when the value has a keycap sequence then it is rejected`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertTrue(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "#", "*", "Apt #12*"])
    fun `when the value has a plain digit or hash or asterisk then it is allowed`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertFalse(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Main\nStreet", "Main\tStreet", "\u0000", "\u0008", "\u001B"])
    fun `when the value has a control character then it is rejected`(value: String) {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters(value)

        // THEN
        assertTrue(result)
    }

    @Test
    fun `when the value has a zero width joiner then it is rejected`() {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters("Main\u200DStreet")

        // THEN
        assertTrue(result)
    }

    @Test
    fun `when the value is empty then it is allowed`() {
        // WHEN
        val result = AddressCharacters.containsInvalidCharacters("")

        // THEN
        assertFalse(result)
    }
}
