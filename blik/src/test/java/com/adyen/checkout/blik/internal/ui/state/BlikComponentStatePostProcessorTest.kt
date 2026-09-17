/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.isErrorVisible
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

/**
 * The post processor runs after the validator, so every state here is written as it would be once validation has
 * run: a field that should count as unfilled carries an error.
 */
internal class BlikComponentStatePostProcessorTest {

    private val postProcessor = BlikComponentStatePostProcessor()

    @Test
    fun `when the initial state is created, then the blik code is asked for focus`() {
        val state = createInitialState().copy(blikCode = invalidField())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(BlikFormElementId.BLIK_CODE), actual.focusRequest)
    }

    @Test
    fun `when the field loses focus, then an error it was holding back is shown`() {
        val state = createInitialState().copy(blikCode = invalidField())

        val actual = process(
            state,
            BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = false),
        )

        assertTrue(actual.blikCode.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the field, then a visible error is hidden`() {
        val state = createInitialState().copy(blikCode = invalidField(isErrorVisible = true))

        val actual = process(
            state,
            BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = true),
        )

        assertFalse(actual.blikCode.isErrorVisible)
    }

    @Test
    fun `when pay focuses the field, then the error it just revealed stays visible`() {
        val state = createInitialState().copy(
            blikCode = invalidField(isErrorVisible = true),
            focusRequest = FocusRequest(BlikFormElementId.BLIK_CODE, showErrorIfPresent = true),
        )

        val actual = process(
            state,
            BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = true),
        )

        assertTrue(actual.blikCode.isErrorVisible)
    }

    @Test
    fun `when a focus request could not be fulfilled, then reporting it back clears it`() {
        val state = createInitialState().copy(focusRequest = FocusRequest(BlikFormElementId.BLIK_CODE))

        val actual = process(state, BlikIntent.FocusRequestConsumed(BlikFormElementId.BLIK_CODE))

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the blik code is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(blikCode = invalidField())

        val actual = process(state, BlikIntent.HighlightValidationErrors)

        assertTrue(actual.blikCode.isErrorVisible)
        assertEquals(FocusRequest(BlikFormElementId.BLIK_CODE, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the blik code is valid, then nothing is shown and no focus is asked for`() {
        val state = createInitialState()

        val actual = process(state, BlikIntent.HighlightValidationErrors)

        assertFalse(actual.blikCode.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the intent decides no focus, then the state is untouched`() {
        val state = createInitialState().copy(blikCode = invalidField())

        val actual = process(state, BlikIntent.UpdateLoading(true))

        assertEquals(state, actual)
    }

    private fun process(state: BlikComponentState, intent: BlikIntent) =
        postProcessor.process(state, state, intent)

    private fun invalidField(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.BLIK_CODE_INVALID, isErrorVisible),
    )

    private fun createInitialState() = BlikComponentState(
        blikCode = TextInputComponentState(),
        isLoading = false,
    )
}
