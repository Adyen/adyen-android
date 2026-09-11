/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewStateTest.TestFormElementId.FIRST
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewStateTest.TestFormElementId.LAST
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
            error = visibleError(),
        )

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST, TestTrailingIcon)

        // THEN
        assertEquals(TrailingIcon.Error, viewState.trailingIcon)
    }

    @Test
    fun `when component state has an error that is not shown yet, then trailing icon is the custom icon`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "invalid",
            error = hiddenError(),
        )

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST, TestTrailingIcon)

        // THEN
        assertEquals(TestTrailingIcon, viewState.trailingIcon)
    }

    @Test
    fun `when component state has no custom icon, then trailing icon is Empty`() {
        // GIVEN
        val componentState = TextInputComponentState(text = "1234")

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertEquals(TrailingIcon.Empty, viewState.trailingIcon)
    }

    @Test
    fun `when the error is visible, then it is shown as supporting text`() {
        // GIVEN
        val componentState = TextInputComponentState(text = "invalid", error = visibleError())

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertEquals(true, viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState.supportingText)
    }

    @Test
    fun `when the error is hidden, then the description is shown as supporting text`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "invalid",
            description = CheckoutLocalizationKey.CARD_NUMBER,
            error = hiddenError(),
        )

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertEquals(false, viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER, viewState.supportingText)
    }

    @Test
    fun `when there is a description and no error, then the description is shown as supporting text`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "text",
            description = CheckoutLocalizationKey.CARD_NUMBER,
            error = null,
        )

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertEquals(false, viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER, viewState.supportingText)
    }

    @Test
    fun `when there is a description and a visible error, then the error takes precedence`() {
        // GIVEN
        val componentState = TextInputComponentState(
            text = "invalid",
            description = CheckoutLocalizationKey.CARD_NUMBER,
            error = visibleError(),
        )

        // WHEN
        val viewState = componentState.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertEquals(true, viewState.isError)
        assertEquals(CheckoutLocalizationKey.CARD_NUMBER_INVALID, viewState.supportingText)
    }

    // A hidden field never reaches this mapping at all: it is not one of its form's elements, so nothing is built.
    // Each component's view state producer test covers that.

    @Test
    fun `when another text input follows, then the keyboard action is next`() {
        // GIVEN
        val form = formOf(FIRST, LAST)

        // WHEN
        val viewState = TextInputComponentState().toViewState(form, null, FIRST)

        // THEN
        assertEquals(KeyboardAction.NEXT, viewState.keyboardAction)
    }

    @Test
    fun `when no text input follows, then the keyboard action is done`() {
        // GIVEN
        val form = formOf(FIRST, LAST)

        // WHEN
        val viewState = TextInputComponentState().toViewState(form, null, LAST)

        // THEN
        assertEquals(KeyboardAction.DONE, viewState.keyboardAction)
    }

    @Test
    fun `when focus is requested for the field, then the view state contains a focus token`() {
        // GIVEN
        val form = formOf(FIRST)

        // WHEN
        val viewState = TextInputComponentState().toViewState(form, FocusRequest(FIRST), FIRST)

        // THEN
        assertNotNull(viewState.focusRequest)
    }

    @Test
    fun `when focus is requested for another field, then the view state has no focus token`() {
        // GIVEN
        val form = formOf(FIRST, LAST)

        // WHEN
        val viewState = TextInputComponentState().toViewState(form, FocusRequest(LAST), FIRST)

        // THEN
        assertNull(viewState.focusRequest)
    }

    @Test
    fun `when field is optional, then view state should exist`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val viewState = state.toViewState(formOf(FIRST), null, FIRST)

        // THEN
        assertNotNull(viewState)
    }

    private fun hiddenError() = TextInputComponentState.InputError(
        message = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
        isVisible = false,
    )

    private fun visibleError() = TextInputComponentState.InputError(
        message = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
        isVisible = true,
    )

    private fun formOf(vararg ids: TestFormElementId) =
        FormState(elements = ids.map { FormElementState(it, isValid = true) })

    private enum class TestFormElementId(override val isTextInput: Boolean = true) : FormElementId {
        FIRST,
        LAST,
    }

    private data object TestTrailingIcon : TrailingIcon()
}
