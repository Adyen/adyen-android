/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.NUMBER
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.STORE_DETAILS
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.VERIFICATION_CODE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests `FormStateExt.kt`: the pure derivations every one of the form behaviours is built on. A field that is not a
 * text input, such as [STORE_DETAILS], takes part in the order but never in a keyboard decision.
 */
internal class FormStateExtTest {

    @Nested
    inner class NextTextInputAfterTest {

        @Test
        fun `when a text input follows, then it is returned`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE, HOLDER_NAME)

            // WHEN
            val next = form.nextTextInputAfter(NUMBER)

            // THEN
            assertEquals(VERIFICATION_CODE, next)
        }

        @Test
        fun `when the next field is not a text input, then it is skipped`() {
            // GIVEN
            val form = formOf(NUMBER, STORE_DETAILS, HOLDER_NAME)

            // WHEN
            val next = form.nextTextInputAfter(NUMBER)

            // THEN
            assertEquals(HOLDER_NAME, next)
        }

        @Test
        fun `when nothing follows the field, then null is returned`() {
            // GIVEN
            val form = formOf(NUMBER, HOLDER_NAME)

            // WHEN
            val next = form.nextTextInputAfter(HOLDER_NAME)

            // THEN
            assertNull(next)
        }

        /**
         * The field can be hidden by configuration, in which case it is not part of the order at all.
         */
        @Test
        fun `when the field is not part of the form, then null is returned`() {
            // GIVEN
            val form = formOf(NUMBER, HOLDER_NAME)

            // WHEN
            val next = form.nextTextInputAfter(VERIFICATION_CODE)

            // THEN
            assertNull(next)
        }
    }

    @Nested
    inner class KeyboardActionForTest {

        @Test
        fun `when the field is not the last text input, then the keyboard moves to the next field`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE)

            // WHEN
            val keyboardAction = form.keyboardActionFor(NUMBER)

            // THEN
            assertEquals(KeyboardAction.NEXT, keyboardAction)
        }

        @Test
        fun `when the field is the last text input, then the keyboard closes`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE)

            // WHEN
            val keyboardAction = form.keyboardActionFor(VERIFICATION_CODE)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }

        /**
         * A field that is not a text input can be the last field on screen without taking the closing action away from
         * the text input before it.
         */
        @Test
        fun `when a field that is not a text input follows the last text input, then the text input still closes`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE, STORE_DETAILS)

            // WHEN
            val keyboardAction = form.keyboardActionFor(VERIFICATION_CODE)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }

        /**
         * A field that is not part of the form has no field after it to move to either, but nothing renders such a
         * field, so this only pins down that the lookup does not fail.
         */
        @Test
        fun `when the field is not part of the form, then the keyboard moves on`() {
            // GIVEN
            val form = formOf(NUMBER)

            // WHEN
            val keyboardAction = form.keyboardActionFor(HOLDER_NAME)

            // THEN
            assertEquals(KeyboardAction.NEXT, keyboardAction)
        }

        @Test
        fun `when the form has a single text input, then it closes the keyboard`() {
            // GIVEN
            val form = formOf(NUMBER)

            // WHEN
            val keyboardAction = form.keyboardActionFor(NUMBER)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }
    }

    /**
     * Which element pay sends focus to, and that it arrives carrying `keepErrorHighlight` so the error it just
     * revealed survives the move.
     */
    @Nested
    inner class RequestFocusOnFirstInvalidTest {

        @Test
        fun `when several elements are invalid, then the first one in visual order is asked for focus`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(VERIFICATION_CODE), invalid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertEquals(FocusRequest(VERIFICATION_CODE, keepErrorHighlight = true), request)
        }

        /**
         * No element that is not a text input can currently be invalid, but the lookup is keyed on any id so that the
         * first one that can does not need new logic.
         */
        @Test
        fun `when the invalid element is not a text input, then it is still asked for focus`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(STORE_DETAILS), valid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertEquals(FocusRequest(STORE_DETAILS, keepErrorHighlight = true), request)
        }

        @Test
        fun `when the form is empty, then no focus is asked for`() {
            // GIVEN
            val form = formOf()

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertNull(request)
        }

        @Test
        fun `when every element is valid, then no focus is asked for`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE)

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertNull(request)
        }
    }

    // Most rules here do not care about validity, so the common helper takes bare ids and treats them all as valid.
    private fun formOf(vararg ids: TestFormElementId) = FormState(elements = ids.map { valid(it) })

    private fun valid(id: TestFormElementId) = FormElementState(id, isValid = true)

    private fun invalid(id: TestFormElementId) = FormElementState(id, isValid = false)

    private enum class TestFormElementId(override val isTextInput: Boolean) : FormElementId {
        NUMBER(true),
        VERIFICATION_CODE(true),
        HOLDER_NAME(true),
        STORE_DETAILS(false),
    }
}
