/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LineBreakToSpaceInputTransformationTest {

    @ParameterizedTest
    @MethodSource("lineBreakSource")
    fun `when the input has a line break or a tab then it becomes a space`(input: String, expected: String) {
        // GIVEN
        val state = TextFieldState()

        // WHEN
        state.edit {
            append(input)
            with(LineBreakToSpaceInputTransformation) { transformInput() }
        }

        // THEN
        assertEquals(expected, state.text.toString())
    }

    @Test
    fun `when the input has no line break then it is unchanged`() {
        // GIVEN
        val state = TextFieldState()

        // WHEN
        state.edit {
            append("Main Street 1")
            with(LineBreakToSpaceInputTransformation) { transformInput() }
        }

        // THEN
        assertEquals("Main Street 1", state.text.toString())
    }

    @Test
    fun `when a line break is entered in the middle then the cursor stays after it`() {
        // GIVEN
        val state = TextFieldState("Main Street 1Apt 4")

        // WHEN
        state.edit {
            insert(13, "\n")
            selection = TextRange(14)
            with(LineBreakToSpaceInputTransformation) { transformInput() }
        }

        // THEN
        assertEquals("Main Street 1 Apt 4", state.text.toString())
        assertEquals(TextRange(14), state.selection)
    }

    @Test
    fun `when a Windows line break is pasted then the cursor stays at the end of the paste`() {
        // GIVEN
        val state = TextFieldState()

        // WHEN
        state.edit {
            append("Main Street 1\r\nApt 4")
            selection = TextRange(length)
            with(LineBreakToSpaceInputTransformation) { transformInput() }
        }

        // THEN
        assertEquals("Main Street 1 Apt 4", state.text.toString())
        assertEquals(TextRange(19), state.selection)
    }

    companion object {

        @JvmStatic
        fun lineBreakSource() = listOf(
            arguments("Main Street 1\nApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\rApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\r\nApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\n\nApt 4", "Main Street 1  Apt 4"),
            arguments("Main Street 1\tApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\u000BApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\u000CApt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\u0085Apt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\u2028Apt 4", "Main Street 1 Apt 4"),
            arguments("Main Street 1\u2029Apt 4", "Main Street 1 Apt 4"),
        )
    }
}
