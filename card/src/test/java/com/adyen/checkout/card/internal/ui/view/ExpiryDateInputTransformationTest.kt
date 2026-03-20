/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class ExpiryDateInputTransformationTest {

    private val transformation = ExpiryDateInputTransformation()

    @Test
    fun `when input is valid then it is accepted`() {
        val state = TextFieldState("12")
        state.edit {
            with(transformation) { transformInput() }
        }
        assertEquals("12", state.text.toString())
    }

    @Test
    fun `when input has illegal characters then they are rejected`() {
        val state = TextFieldState("12")
        state.edit {
            append("a")
            with(transformation) { transformInput() }
        }
        assertEquals("12", state.text.toString())
    }

    @Test
    fun `when input has valid separators then inout is accepted and separators are removed`() {
        val state = TextFieldState("12/3", TextRange(4))

        state.edit {
            with(transformation) { transformInput() }
        }

        assertEquals("123", state.text.toString())
    }

    @Test
    fun `when input exceeds max length then it is rejected`() {
        val state = TextFieldState("1234")
        state.edit {
            append("5")
            with(transformation) { transformInput() }
        }
        assertEquals("1234", state.text.toString())
    }

    @ParameterizedTest
    @MethodSource("autoAppendZeroSource")
    fun `when input is one digit only then 0 is appended at the start if that digit is not 0 or 1`(
        initialText: String,
        transformedText: String,
    ) {
        val state = TextFieldState("")
        state.edit {
            append(initialText)
            with(transformation) { transformInput() }
        }
        assertEquals(transformedText, state.text.toString())
    }

    companion object {
        @JvmStatic
        fun autoAppendZeroSource() = listOf(
            // initialText, transformedText
            arguments("0", "0"),
            arguments("1", "1"),
            arguments("2", "02"),
            arguments("3", "03"),
            arguments("4", "04"),
            arguments("5", "05"),
            arguments("6", "06"),
            arguments("7", "07"),
            arguments("8", "08"),
            arguments("9", "09"),
        )
    }
}
