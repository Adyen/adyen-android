/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.NUMBER
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.STORE_DETAILS
import com.adyen.checkout.core.components.internal.ui.state.form.FormStateExtTest.TestFormElementId.VERIFICATION_CODE
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FormStateExtTest {

    @Nested
    inner class FormValidityTest {

        @Test
        fun `when every visible element is valid, then the form is valid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), valid(HOLDER_NAME)))

            // WHEN
            val isFormValid = form.isFormValid

            // THEN
            assertTrue(isFormValid)
        }

        @Test
        fun `when a visible element is invalid, then the form is invalid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(HOLDER_NAME)))

            // WHEN
            val isFormValid = form.isFormValid

            // THEN
            assertFalse(isFormValid)
        }

        @Test
        fun `when an element is absent, then it is not visible and valid`() {
            // GIVEN
            val form = formOf(NUMBER)

            // WHEN
            val isVisibleAndValid = form.isElementVisibleAndValid(HOLDER_NAME)

            // THEN
            assertFalse(isVisibleAndValid)
        }
    }

    @Nested
    inner class FormElementStateTest {

        @Test
        fun `when a text input is visible, then its validity is represented in the form`() {
            // GIVEN
            val field = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.CARD_NUMBER_INVALID),
            )

            // WHEN
            val element = field.toFormElementIfVisible(NUMBER)

            // THEN
            assertEquals(FormElementState(NUMBER, isValid = false), element)
        }

        @Test
        fun `when a text input is hidden, then it is absent from the form`() {
            // GIVEN
            val field = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden)

            // WHEN
            val element = field.toFormElementIfVisible(NUMBER)

            // THEN
            assertNull(element)
        }
    }

    @Nested
    inner class RequestFocusOnFirstTextInputTest {

        @Test
        fun `when the form starts with a non text input, then the first text input is requested`() {
            // GIVEN
            val form = formOf(STORE_DETAILS, NUMBER)

            // WHEN
            val request = form.requestFocusOnFirstTextInput()

            // THEN
            assertEquals(FocusRequest(NUMBER), request)
        }

        @Test
        fun `when the form has no text input, then no focus is requested`() {
            // GIVEN
            val form = formOf(STORE_DETAILS)

            // WHEN
            val request = form.requestFocusOnFirstTextInput()

            // THEN
            assertNull(request)
        }
    }

    @Nested
    inner class FormOrderTest {

        @Test
        fun `when a non text input follows, then it is skipped when finding the next text input`() {
            // GIVEN
            val form = formOf(NUMBER, STORE_DETAILS, HOLDER_NAME)

            // WHEN
            val next = form.nextTextInputAfter(NUMBER)

            // THEN
            assertEquals(HOLDER_NAME, next)
        }

        @Test
        fun `when a field is last or absent, then no next text input exists`() {
            // GIVEN
            val form = formOf(NUMBER, HOLDER_NAME)

            // WHEN
            val afterLast = form.nextTextInputAfter(HOLDER_NAME)
            val afterAbsent = form.nextTextInputAfter(VERIFICATION_CODE)

            // THEN
            assertNull(afterLast)
            assertNull(afterAbsent)
        }

        @Test
        fun `when a non text input follows the last text input, then the text input uses done`() {
            // GIVEN
            val form = formOf(NUMBER, STORE_DETAILS)

            // WHEN
            val action = form.keyboardActionFor(NUMBER)

            // THEN
            assertEquals(KeyboardAction.DONE, action)
        }

        @Test
        fun `when another text input follows, then the field uses next`() {
            // GIVEN
            val form = formOf(NUMBER, HOLDER_NAME)

            // WHEN
            val action = form.keyboardActionFor(NUMBER)

            // THEN
            assertEquals(KeyboardAction.NEXT, action)
        }
    }

    @Nested
    inner class RequestFocusOnFirstInvalidTest {

        @Test
        fun `when several elements are invalid, then the first one is requested with its error visible`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(VERIFICATION_CODE), invalid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertEquals(FocusRequest(VERIFICATION_CODE, showErrorIfPresent = true), request)
        }

        @Test
        fun `when the form is empty, then no focus is requested`() {
            // GIVEN
            val form = formOf()

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertNull(request)
        }

        @Test
        fun `when every element is valid, then no focus is requested`() {
            // GIVEN
            val form = formOf(NUMBER, HOLDER_NAME)

            // WHEN
            val request = form.requestFocusOnFirstInvalid()

            // THEN
            assertNull(request)
        }
    }

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
