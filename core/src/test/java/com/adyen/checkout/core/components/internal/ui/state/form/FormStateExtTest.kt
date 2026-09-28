/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.NUMBER
import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.STORE_DETAILS
import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.VERIFICATION_CODE
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FormStateExtTest {

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
    inner class FormOrderTest {

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

        @Test
        fun `when the field is not part of the form, then the keyboard moves on`() {
            // GIVEN
            val form = formOf(NUMBER)

            // WHEN
            val action = form.keyboardActionFor(HOLDER_NAME)

            // THEN
            assertEquals(KeyboardAction.NEXT, action)
        }
    }

    @Nested
    inner class RequestFocusOnFirstInvalidTextInputTest {

        @Test
        fun `when valid fields are prefilled, then the first invalid text input is requested`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(VERIFICATION_CODE), invalid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = false)

            // THEN
            assertEquals(FocusRequest(VERIFICATION_CODE, showErrorIfPresent = false), request)
        }

        @Test
        fun `when the error should be shown, then the request shows it`() {
            // GIVEN
            val form = FormState(elements = listOf(invalid(NUMBER)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = true)

            // THEN
            assertEquals(FocusRequest(NUMBER, showErrorIfPresent = true), request)
        }

        @Test
        fun `when an invalid non text input comes first, then it is skipped`() {
            // GIVEN
            val form = FormState(elements = listOf(invalid(STORE_DETAILS), invalid(NUMBER)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = true)

            // THEN
            assertEquals(FocusRequest(NUMBER, showErrorIfPresent = true), request)
        }

        @Test
        fun `when there is no invalid text input, then no focus is requested`() {
            // GIVEN
            val form = FormState(elements = listOf(invalid(STORE_DETAILS), valid(NUMBER)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = false)

            // THEN
            assertNull(request)
        }
    }

    @Nested
    inner class RequestFocusOnFirstInvalidTextInputAfterElementTest {

        @Test
        fun `when valid and non text elements follow, then the first invalid text input is requested`() {
            // GIVEN
            val form = FormState(
                elements = listOf(
                    valid(NUMBER),
                    valid(VERIFICATION_CODE),
                    invalid(STORE_DETAILS),
                    invalid(HOLDER_NAME),
                ),
            )

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(
                showErrorIfPresent = false,
                afterElementId = NUMBER,
            )

            // THEN
            assertEquals(FocusRequest(HOLDER_NAME), request)
        }

        @Test
        fun `when an invalid text input comes before the current element, then it is not requested`() {
            // GIVEN
            val form = FormState(elements = listOf(invalid(NUMBER), valid(VERIFICATION_CODE), valid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(
                showErrorIfPresent = false,
                afterElementId = VERIFICATION_CODE,
            )

            // THEN
            assertNull(request)
        }

        @Test
        fun `when no invalid text input follows, then no focus is requested`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(VERIFICATION_CODE)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(
                showErrorIfPresent = false,
                afterElementId = VERIFICATION_CODE,
            )

            // THEN
            assertNull(request)
        }

        @Test
        fun `when the after element is absent, then the first invalid input is requested`() {
            // GIVEN
            val form = FormState(elements = listOf(invalid(HOLDER_NAME)))

            // WHEN
            val request = form.requestFocusOnFirstInvalidTextInput(
                showErrorIfPresent = false,
                afterElementId = NUMBER,
            )

            // THEN
            assertEquals(FocusRequest(HOLDER_NAME), request)
        }
    }
}
