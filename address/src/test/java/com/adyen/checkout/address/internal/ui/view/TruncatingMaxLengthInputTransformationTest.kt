/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TruncatingMaxLengthInputTransformationTest {

    private val transformation = TruncatingMaxLengthInputTransformation(maxLength = 7)

    @Test
    fun `when text is typed within the maximum then it is kept`() {
        // GIVEN
        val state = TextFieldState("1011")

        // WHEN
        state.edit {
            append(" D")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("1011 D", state.text.toString())
    }

    @Test
    fun `when typing would exceed the maximum then nothing is added`() {
        // GIVEN
        val state = TextFieldState("1011 DJ")

        // WHEN
        state.edit {
            append("X")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("1011 DJ", state.text.toString())
    }

    @Test
    fun `when a paste exceeds the maximum then it is cut to fit`() {
        // GIVEN
        val state = TextFieldState("10")

        // WHEN
        state.edit {
            append("11 DJ AMSTERDAM")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("1011 DJ", state.text.toString())
    }

    @Test
    fun `when a paste in the middle exceeds the maximum then the end of the existing text is kept`() {
        // GIVEN
        val state = TextFieldState("10DJ")

        // WHEN
        state.edit {
            insert(2, "11 XYZ")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("1011 DJ", state.text.toString())
    }

    @Test
    fun `when a paste is cut in the middle of an emoji then the whole emoji is removed`() {
        // GIVEN
        val state = TextFieldState()

        // WHEN
        state.edit {
            append("Street😀")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("Street", state.text.toString())
    }

    @Test
    fun `when an emoji fits within the maximum then it is kept whole`() {
        // GIVEN
        val state = TextFieldState()

        // WHEN
        state.edit {
            append("Stree😀")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("Stree😀", state.text.toString())
    }

    @Test
    fun `when a paste is cut then the cursor ends after the kept text`() {
        // GIVEN
        val state = TextFieldState("10DJ")

        // WHEN
        state.edit {
            insert(2, "11 XYZ")
            selection = TextRange(8)
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals(TextRange(5), state.selection)
    }

    @Test
    fun `when the text is already over the maximum then deleting is allowed`() {
        // GIVEN
        val state = TextFieldState("12345-6789")

        // WHEN
        state.edit {
            delete(length - 1, length)
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("12345-678", state.text.toString())
    }

    @Test
    fun `when the text is already over the maximum then typing adds nothing`() {
        // GIVEN
        val state = TextFieldState("12345-6789")

        // WHEN
        state.edit {
            append("0")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("12345-6789", state.text.toString())
    }
}
