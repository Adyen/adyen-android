/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFieldId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFieldId.NUMBER
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFieldId.STORE_DETAILS
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFieldId.VERIFICATION_CODE
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
    inner class LastTextInputTest {

        @Test
        fun `when the form has several text inputs, then the last one is returned`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE, HOLDER_NAME)

            // WHEN
            val lastTextInput = form.lastTextInput()

            // THEN
            assertEquals(HOLDER_NAME, lastTextInput)
        }

        @Test
        fun `when the form ends with a field that is not a text input, then the last text input is returned`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE, STORE_DETAILS)

            // WHEN
            val lastTextInput = form.lastTextInput()

            // THEN
            assertEquals(VERIFICATION_CODE, lastTextInput)
        }

        @Test
        fun `when the form has no text input at all, then null is returned`() {
            // GIVEN
            val form = formOf(STORE_DETAILS)

            // WHEN
            val lastTextInput = form.lastTextInput()

            // THEN
            assertNull(lastTextInput)
        }

        @Test
        fun `when the form is empty, then null is returned`() {
            // GIVEN
            val form = formOf()

            // WHEN
            val lastTextInput = form.lastTextInput()

            // THEN
            assertNull(lastTextInput)
        }
    }

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

    @Nested
    inner class FirstInvalidTest {

        @Test
        fun `when several fields are invalid, then the first one in visual order is returned`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE, HOLDER_NAME)

            // WHEN
            val firstInvalid = form.firstInvalid { it != VERIFICATION_CODE && it != HOLDER_NAME }

            // THEN
            assertEquals(VERIFICATION_CODE, firstInvalid)
        }

        @Test
        fun `when every field is valid, then null is returned`() {
            // GIVEN
            val form = formOf(NUMBER, VERIFICATION_CODE)

            // WHEN
            val firstInvalid = form.firstInvalid { true }

            // THEN
            assertNull(firstInvalid)
        }

        /**
         * No field that is not a text input can currently be invalid, but the lookup is keyed on any id so that the
         * first one that can does not need new logic.
         */
        @Test
        fun `when the invalid field is not a text input, then it is still returned`() {
            // GIVEN
            val form = formOf(NUMBER, STORE_DETAILS, HOLDER_NAME)

            // WHEN
            val firstInvalid = form.firstInvalid { it != STORE_DETAILS }

            // THEN
            assertEquals(STORE_DETAILS, firstInvalid)
        }

        @Test
        fun `when the form is empty, then null is returned`() {
            // GIVEN
            val form = formOf()

            // WHEN
            val firstInvalid = form.firstInvalid { false }

            // THEN
            assertNull(firstInvalid)
        }
    }

    private fun formOf(vararg ids: TestFieldId) = FormState(order = ids.toList())

    private enum class TestFieldId(override val isTextInput: Boolean) : FormFieldId {
        NUMBER(true),
        VERIFICATION_CODE(true),
        HOLDER_NAME(true),
        STORE_DETAILS(false),
    }
}
