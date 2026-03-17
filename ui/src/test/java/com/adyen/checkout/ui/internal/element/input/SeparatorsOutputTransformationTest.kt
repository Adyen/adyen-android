/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.compose.foundation.text.input.TextFieldState
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class SeparatorsOutputTransformationTest {

    @Test
    fun `when transforming empty text then output is empty`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('.', 1),
            ),
        )
        val state = TextFieldState("")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("", state.text.toString())
    }

    @Test
    fun `when transforming text with no separators then output is the same`() {
        val outputTransformation = SeparatorsOutputTransformation(emptyList())
        val state = TextFieldState("1234567")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("1234567", state.text.toString())
    }

    @Test
    fun `when transforming text with separator then output is matching`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator(' ', 3),
            ),
        )
        val state = TextFieldState("123456")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("123 456", state.text.toString())
    }

    @Test
    fun `when transforming text with separator at the start then output is matching`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('.', 0),
            ),
        )
        val state = TextFieldState("124")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals(".124", state.text.toString())
    }

    @Test
    fun `when transforming text with separator at the end then output does not have separators`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('-', 3),
            ),
        )
        val state = TextFieldState("124")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("124", state.text.toString())
    }

    @Test
    fun `when transforming text shorter than separator index then output does not have separators`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('/', 6),
            ),
        )
        val state = TextFieldState("abc")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("abc", state.text.toString())
    }

    @Test
    fun `when transforming text with multiple separators then output should have these separators`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('.', 2),
                TextFieldSeparator('-', 5),
            ),
        )
        val state = TextFieldState("123456789")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("12.345-6789", state.text.toString())
    }

    @Test
    fun `when transforming shorter text with multiple separators then output should have the first separators`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator(' ', 3),
                TextFieldSeparator(' ', 6),
            ),
        )
        val state = TextFieldState("123456")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("123 456", state.text.toString())
    }

    @Test
    fun `when separators have duplicate indexes then error should be thrown`() {
        assertThrows<IllegalStateException> {
            val outputTransformation = SeparatorsOutputTransformation(
                listOf(
                    TextFieldSeparator('.', 3),
                    TextFieldSeparator('-', 3),
                    TextFieldSeparator('-', 5),
                ),
            )
            val state = TextFieldState("123456789")
            state.edit {
                with(outputTransformation) { transformOutput() }
            }
        }
    }

    @Test
    fun `when separators have unsorted indexes then output should be correctly formatted`() {
        val outputTransformation = SeparatorsOutputTransformation(
            listOf(
                TextFieldSeparator('-', 5),
                TextFieldSeparator('/', 9),
                TextFieldSeparator('.', 3),
            ),
        )
        val state = TextFieldState("1234567890")
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals("123.45-6789/0", state.text.toString())
    }
}
