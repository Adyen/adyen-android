/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DigitOnlyInputTransformationTest {

    @Test
    fun `when input is valid then it is accepted`() {
        val state = TextFieldState("123")
        state.edit {
            with(DigitOnlyInputTransformation()) { transformInput() }
        }
        assertEquals("123", state.text.toString())
    }

    @Test
    fun `when input has illegal characters then they are rejected`() {
        val state = TextFieldState("12")
        state.edit {
            append("a")
            with(DigitOnlyInputTransformation(allowedSeparators = listOf(' '))) { transformInput() }
        }
        assertEquals("12", state.text.toString())
    }

    @Test
    fun `when input is modified in the middle then cursor is preserved`() {
        // GIVEN
        // 12345 with cursor at index 2
        val state = TextFieldState("12345", TextRange(2))

        // WHEN
        // 0 inserted at index 2
        state.edit {
            insert(2, "0")
            with(DigitOnlyInputTransformation()) { transformInput() }
        }

        // THEN
        // 120345 -> cursor should be at index 3 because 0 was added there
        assertEquals("120345", state.text.toString())
        assertEquals(3, state.selection.start)
    }

    @Test
    fun `when input has valid separators then separators are removed and cursor is preserved`() {
        // GIVEN
        // 12.3 -> cursor at index 4
        val state = TextFieldState("12.3", TextRange(4))

        // WHEN
        state.edit {
            with(DigitOnlyInputTransformation(allowedSeparators = listOf('.'))) { transformInput() }
        }

        // THEN
        // 123 -> cursor should be at index 3 because '.' at index 2 was removed
        assertEquals("123", state.text.toString())
        assertEquals(3, state.selection.start)
    }

    @Test
    fun `when input exceeds max length then it is rejected`() {
        val state = TextFieldState("12345678901234")
        state.edit {
            append("5")
            with(DigitOnlyInputTransformation(maxLengthWithoutSeparators = 14)) { transformInput() }
        }
        assertEquals("12345678901234", state.text.toString())
    }
}
