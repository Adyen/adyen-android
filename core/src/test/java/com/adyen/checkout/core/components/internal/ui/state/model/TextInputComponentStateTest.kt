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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TextInputComponentStateTest {

    // UC1: Error on Explicit Validation
    @Test
    fun `when state has error message and showError is true, then error is displayed`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
        )

        // WHEN
        val viewState = state.toViewState()

        // THEN
        assertTrue(viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState.supportingText)
    }

    // UC1: Error on Explicit Validation - verify isValid
    @Test
    fun `when state has error message, then isValid returns false`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
        )

        // WHEN
        val isValid = state.isValid

        // THEN
        assertFalse(isValid)
    }

    // UC2: Error Cleared on Focus
    @Test
    fun `when field gains focus, then showError is set to false`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
            isFocused = false,
        )

        // WHEN
        val updatedState = state.updateFocus(hasFocus = true)

        // THEN
        assertFalse(updatedState.showError)
        assertTrue(updatedState.isFocused)
    }

    // UC2: Error Cleared on Focus - verify view state
    @Test
    fun `when field gains focus with error, then error is not displayed in view state`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
        )

        // WHEN
        val focusedState = state.updateFocus(hasFocus = true)
        val viewState = focusedState.toViewState()

        // THEN
        assertFalse(viewState.isError)
    }

    // UC3: Error on Focus Loss
    @Test
    fun `when field loses focus, then showError is set to true`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            isFocused = true,
            showError = false,
        )

        // WHEN
        val updatedState = state.updateFocus(hasFocus = false)

        // THEN
        assertTrue(updatedState.showError)
        assertFalse(updatedState.isFocused)
    }

    // UC3: Error on Focus Loss - verify view state displays error
    @Test
    fun `when field loses focus with invalid input, then error is displayed in view state`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            isFocused = true,
        )

        // WHEN
        val blurredState = state.updateFocus(hasFocus = false)
        val viewState = blurredState.toViewState()

        // THEN
        assertTrue(viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState.supportingText)
    }

    // UC4: No Error While Typing
    @Test
    fun `when text is updated, then showError is set to false`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "old",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
        )

        // WHEN
        val updatedState = state.updateText("new")

        // THEN
        assertFalse(updatedState.showError)
        assertEquals("new", updatedState.text)
    }

    // UC4: No Error While Typing - verify error not shown even if errorMessage exists
    @Test
    fun `when user is typing with error message present, then error is not displayed in view state`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            isFocused = true,
        )

        // WHEN
        val typingState = state.updateText("still_invalid")
        val viewState = typingState.toViewState()

        // THEN
        assertFalse(viewState.isError)
        assertFalse(typingState.showError)
    }

    // UC14: Empty Field - No Error on Focus Loss
    @Test
    fun `when empty field loses focus, then no error is shown`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "",
            errorMessage = null,
            isFocused = true,
        )

        // WHEN
        val blurredState = state.updateFocus(hasFocus = false)
        val viewState = blurredState.toViewState()

        // THEN
        assertFalse(viewState.isError)
        assertTrue(state.isValid)
    }

    // UC14: Empty Field - verify empty is considered valid
    @Test
    fun `when field is empty with no error message, then isValid returns true`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "",
            errorMessage = null,
        )

        // WHEN
        val isValid = state.isValid

        // THEN
        assertTrue(isValid)
    }

    // Additional test: Verify placeholder is shown when no error
    @Test
    fun `when state has description and no error, then description is shown as supporting text`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "text",
            description = CheckoutLocalizationKey.CARD_NUMBER,
            errorMessage = null,
            showError = false,
        )

        // WHEN
        val viewState = state.toViewState()

        // THEN
        assertFalse(viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER, viewState.supportingText)
    }

    // Additional test: Verify error replaces placeholder
    @Test
    fun `when state has both description and error with showError true, then error is shown as supporting text`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "invalid",
            description = CheckoutLocalizationKey.CARD_NUMBER,
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
        )

        // WHEN
        val viewState = state.toViewState()

        // THEN
        assertTrue(viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState.supportingText)
    }
}
