/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by andriim on 9/2/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentStateTest.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentStateTest.TestFormElementId.NUMBER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests `TextInputComponentState.kt`: how the state of a field evolves as the shopper types, focuses and blurs, and
 * when that results in an error being displayed. A few tests observe the outcome through `toViewState`, where the
 * point is the transition rather than the mapping.
 *
 * Tests for the mapping itself live in `TextInputViewStateTest`.
 */
internal class TextInputComponentStateTest {

    @Nested
    inner class IsValidTest {

        // UC1: Error on Explicit Validation - verify isValid
        @Test
        fun `when state has an error, then isValid returns false`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = hiddenError())

            // WHEN
            val isValid = state.isValid

            // THEN
            assertFalse(isValid)
        }

        // UC14: Empty Field - verify empty is considered valid
        @Test
        fun `when field is empty with no error, then isValid returns true`() {
            // GIVEN
            val state = TextInputComponentState(text = "", error = null)

            // WHEN
            val isValid = state.isValid

            // THEN
            assertTrue(isValid)
        }
    }

    /**
     * There are only three states, because [TextInputComponentState.InputError] holds the message and its visibility
     * together. A field that shows an error without having one cannot be built.
     */
    @Nested
    inner class IsErrorVisibleTest {

        @Test
        fun `when there is no error, then it is not visible`() {
            // GIVEN
            val state = TextInputComponentState(error = null)

            // WHEN
            val isErrorVisible = state.isErrorVisible

            // THEN
            assertFalse(isErrorVisible)
        }

        @Test
        fun `when there is a hidden error, then it is not visible`() {
            // GIVEN
            val state = TextInputComponentState(error = hiddenError())

            // WHEN
            val isErrorVisible = state.isErrorVisible

            // THEN
            assertFalse(isErrorVisible)
        }

        @Test
        fun `when there is a visible error, then it is visible`() {
            // GIVEN
            val state = TextInputComponentState(error = visibleError())

            // WHEN
            val isErrorVisible = state.isErrorVisible

            // THEN
            assertTrue(isErrorVisible)
        }
    }

    /**
     * Validators call `updateError` on every pass, including passes triggered by a different field, so it must not
     * disturb whether the shopper is currently looking at the error.
     */
    @Nested
    inner class UpdateErrorTest {

        @Test
        fun `when a field has no error, then the new error starts hidden`() {
            // GIVEN
            val state = TextInputComponentState(error = null)

            // WHEN
            val updatedState = state.updateError(CheckoutLocalizationKey.CARD_NUMBER_INVALID)

            // THEN
            assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, updatedState.error?.message)
            assertFalse(updatedState.isErrorVisible)
        }

        @Test
        fun `when the error is visible, then replacing the message keeps it visible`() {
            // GIVEN
            val state = TextInputComponentState(error = visibleError())

            // WHEN
            val updatedState = state.updateError(CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND)

            // THEN
            assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND, updatedState.error?.message)
            assertTrue(updatedState.isErrorVisible)
        }

        @Test
        fun `when the error is hidden, then replacing the message keeps it hidden`() {
            // GIVEN
            val state = TextInputComponentState(error = hiddenError())

            // WHEN
            val updatedState = state.updateError(CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND)

            // THEN
            assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID_UNSUPPORTED_BRAND, updatedState.error?.message)
            assertFalse(updatedState.isErrorVisible)
        }

        @Test
        fun `when the field becomes valid, then the error is removed`() {
            // GIVEN
            val state = TextInputComponentState(error = visibleError())

            // WHEN
            val updatedState = state.updateError(null)

            // THEN
            assertNull(updatedState.error)
            assertTrue(updatedState.isValid)
        }
    }

    @Nested
    inner class UpdateTextTest {

        // UC4: No Error While Typing
        @Test
        fun `when text is updated, then the error is hidden`() {
            // GIVEN
            val state = TextInputComponentState(text = "old", error = visibleError())

            // WHEN
            val updatedState = state.updateText("new")

            // THEN
            assertEquals("new", updatedState.text)
            assertFalse(updatedState.isErrorVisible)
        }

        // UC4: No Error While Typing - verify error not shown even if an error exists
        @Test
        fun `when user is typing with an error present, then the error is not shown`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError())

            // WHEN
            val typingState = state.updateText("still_invalid")

            // THEN
            // What the UI makes of this is TextInputViewStateTest's business, and it needs a form to answer.
            assertFalse(typingState.isErrorVisible)
        }
    }

    // The rules that decide when a focus change shows or hides an error live in each component's reducer, because only
    // the reducer can tell a focus gain the shopper caused from one a FormState.focusRequest caused. They are tested
    // there. showErrorIfPresent and hideErrorIfPresent, which those rules are built from, are covered below.

    @Nested
    inner class ShowAndHideErrorTest {

        @Test
        fun `when the error is shown on an invalid field, then it becomes visible`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = hiddenError())

            // WHEN
            val updatedState = state.showErrorIfPresent()

            // THEN
            assertTrue(updatedState.isErrorVisible)
        }

        /**
         * A field with nothing wrong with it has nothing to show, so callers do not have to guard against this.
         */
        @Test
        fun `when the error is shown on a valid field, then nothing changes`() {
            // GIVEN
            val state = TextInputComponentState(text = "valid", error = null)

            // WHEN
            val updatedState = state.showErrorIfPresent()

            // THEN
            assertEquals(state, updatedState)
            assertFalse(updatedState.isErrorVisible)
        }

        @Test
        fun `when the error is hidden on a field showing one, then it stops being visible`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError())

            // WHEN
            val updatedState = state.hideErrorIfPresent()

            // THEN
            assertFalse(updatedState.isErrorVisible)
            assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, updatedState.error?.message)
        }

        @Test
        fun `when the error is shown twice, then nothing changes`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError())

            // WHEN
            val updatedState = state.showErrorIfPresent()

            // THEN
            assertEquals(state, updatedState)
        }
    }

    private fun hiddenError() = TextInputComponentState.InputError(
        message = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
        isVisible = false,
    )

    private fun visibleError() = TextInputComponentState.InputError(
        message = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
        isVisible = true,
    )

    @Nested
    inner class ApplyFocusChangeTest {

        @Test
        fun `when a field loses focus, then an error it was holding back is shown`() {
            val field = fieldWithHiddenError().applyFocusChange(noRequest, NUMBER, hasFocus = false)

            assertTrue(field.isErrorVisible)
        }

        @Test
        fun `when the shopper focuses a field, then a visible error is hidden`() {
            val field = fieldWithVisibleError().applyFocusChange(noRequest, NUMBER, hasFocus = true)

            assertFalse(field.isErrorVisible)
        }

        @Test
        fun `when pay asked for this focus, then the error it revealed stays visible`() {
            val request = FocusRequest(NUMBER, keepErrorHighlight = true)

            val field = fieldWithVisibleError().applyFocusChange(request, NUMBER, hasFocus = true)

            assertTrue(field.isErrorVisible)
        }

        @Test
        fun `when a request without the highlight asked for this focus, then the error is hidden like a tap`() {
            // A prefill or auto-advance lands the shopper on a field to type in, so a stale error must not greet them.
            val request = FocusRequest(NUMBER)

            val field = fieldWithVisibleError().applyFocusChange(request, NUMBER, hasFocus = true)

            assertFalse(field.isErrorVisible)
        }

        @Test
        fun `when the request names another field, then this field behaves like a tap`() {
            val request = FocusRequest(HOLDER_NAME, keepErrorHighlight = true)

            val field = fieldWithVisibleError().applyFocusChange(request, NUMBER, hasFocus = true)

            assertFalse(field.isErrorVisible)
        }

        @Test
        fun `when the field has no error, then neither direction invents one`() {
            val field = TextInputComponentState()

            assertFalse(field.applyFocusChange(noRequest, NUMBER, hasFocus = false).isErrorVisible)
            assertFalse(field.applyFocusChange(noRequest, NUMBER, hasFocus = true).isErrorVisible)
        }

        private val noRequest: FocusRequest<TestFormElementId>? = null

        private fun fieldWithHiddenError() = TextInputComponentState(
            error = TextInputComponentState.InputError(CheckoutLocalizationKey.CARD_NUMBER_INVALID),
        )

        private fun fieldWithVisibleError() = TextInputComponentState(
            error = TextInputComponentState.InputError(CheckoutLocalizationKey.CARD_NUMBER_INVALID, isVisible = true),
        )
    }

    private enum class TestFormElementId(override val isTextInput: Boolean = true) : FormElementId {
        NUMBER,
        HOLDER_NAME,
    }
}
