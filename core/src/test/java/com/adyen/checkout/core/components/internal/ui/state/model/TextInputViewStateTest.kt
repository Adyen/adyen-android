/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

/**
 * Tests `TextInputViewState.kt`: the icon [TextInputViewState.trailingIcon] resolves to, and the `toViewState` mapping.
 *
 * Tests for the behaviour of the component state itself live in `TextInputComponentStateTest`.
 */
internal class TextInputViewStateTest {

    @Test
    fun `when there is no error and no custom icon, then trailing icon is Empty`() {
        // GIVEN
        val viewState = TextInputViewState(isError = false, customTrailingIcon = null)

        // WHEN
        val trailingIcon = viewState.trailingIcon

        // THEN
        assertEquals(TrailingIcon.Empty, trailingIcon)
    }

    @Test
    fun `when there is no error and a custom icon, then trailing icon is the custom icon`() {
        // GIVEN
        val viewState = TextInputViewState(isError = false, customTrailingIcon = TestTrailingIcon)

        // WHEN
        val trailingIcon = viewState.trailingIcon

        // THEN
        assertEquals(TestTrailingIcon, trailingIcon)
    }

    @Test
    fun `when there is an error and no custom icon, then trailing icon is Error`() {
        // GIVEN
        val viewState = TextInputViewState(isError = true, customTrailingIcon = null)

        // WHEN
        val trailingIcon = viewState.trailingIcon

        // THEN
        assertEquals(TrailingIcon.Error, trailingIcon)
    }

    @Test
    fun `when there is an error and a custom icon, then the error icon takes precedence`() {
        // GIVEN
        val viewState = TextInputViewState(isError = true, customTrailingIcon = TestTrailingIcon)

        // WHEN
        val trailingIcon = viewState.trailingIcon

        // THEN
        assertEquals(TrailingIcon.Error, trailingIcon)
    }

    @Test
    fun `when component state is showing an error, then trailing icon is Error`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = true,
        )

        // WHEN
        val viewState = componentState.toViewState(customTrailingIcon = TestTrailingIcon)

        // THEN
        assertEquals(TrailingIcon.Error, viewState?.trailingIcon)
    }

    @Test
    fun `when component state has an error that is not shown yet, then trailing icon is the custom icon`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "invalid",
            errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
            showError = false,
        )

        // WHEN
        val viewState = componentState.toViewState(customTrailingIcon = TestTrailingIcon)

        // THEN
        assertEquals(TestTrailingIcon, viewState?.trailingIcon)
    }

    @Test
    fun `when component state has no custom icon, then trailing icon is Empty`() {
        // GIVEN
        val componentState = TextInputComponentState(text = "1234")

        // WHEN
        val viewState = componentState.toViewState()

        // THEN
        assertEquals(TrailingIcon.Empty, viewState?.trailingIcon)
    }

    @Test
    fun `when field is hidden, then view state should be null so it doesn't get displayed`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val viewState = state.toViewState()

        // THEN
        assertNull(viewState)
    }

    @Test
    fun `when field is optional, then view state should exist`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val viewState = state.toViewState()

        // THEN
        assertNotNull(viewState)
    }

    private data object TestTrailingIcon : TrailingIcon()
}
