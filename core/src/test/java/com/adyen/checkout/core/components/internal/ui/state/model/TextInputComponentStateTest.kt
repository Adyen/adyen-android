/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by andriim on 9/2/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
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
        fun `when user is typing with an error present, then error is not displayed in view state`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError(), isFocused = true)

            // WHEN
            val typingState = state.updateText("still_invalid")
            val viewState = typingState.toViewState()

            // THEN
            assertEquals(false, viewState?.isError)
            assertFalse(typingState.isErrorVisible)
        }
    }

    @Nested
    inner class UpdateFocusTest {

        // UC2: Error Cleared on Focus
        @Test
        fun `when field gains focus, then the error is hidden`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError(), isFocused = false)

            // WHEN
            val updatedState = state.updateFocus(hasFocus = true)

            // THEN
            assertFalse(updatedState.isErrorVisible)
            assertTrue(updatedState.isFocused)
        }

        // UC2: Error Cleared on Focus - verify view state
        @Test
        fun `when field gains focus with error, then error is not displayed in view state`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = visibleError())

            // WHEN
            val focusedState = state.updateFocus(hasFocus = true)
            val viewState = focusedState.toViewState()

            // THEN
            assertEquals(false, viewState?.isError)
        }

        // UC3: Error on Focus Loss
        @Test
        fun `when field loses focus, then the error is shown`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = hiddenError(), isFocused = true)

            // WHEN
            val updatedState = state.updateFocus(hasFocus = false)

            // THEN
            assertTrue(updatedState.isErrorVisible)
            assertFalse(updatedState.isFocused)
        }

        // UC3: Error on Focus Loss - verify view state displays error
        @Test
        fun `when field loses focus with invalid input, then error is displayed in view state`() {
            // GIVEN
            val state = TextInputComponentState(text = "invalid", error = hiddenError(), isFocused = true)

            // WHEN
            val blurredState = state.updateFocus(hasFocus = false)
            val viewState = blurredState.toViewState()

            // THEN
            assertEquals(true, viewState?.isError)
            assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState?.supportingText)
        }

        // UC14: Empty Field - No Error on Focus Loss
        @Test
        fun `when a valid field loses focus, then no error is shown`() {
            // GIVEN
            val state = TextInputComponentState(text = "", error = null, isFocused = true)

            // WHEN
            val blurredState = state.updateFocus(hasFocus = false)
            val viewState = blurredState.toViewState()

            // THEN
            assertEquals(false, viewState?.isError)
        }
    }

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
}
