/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

internal class BlikComponentStateReducerTest {

    private lateinit var reducer: BlikComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = BlikComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateBlikCode, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.UpdateBlikCode("123456"))

        val expected = state.copy(blikCode = state.blikCode.copy(text = "123456"))
        assertEquals(expected, actual)
    }

    @Test
    fun `when the field loses focus, then an error it was holding back is shown`() {
        val state = createInitialState().copy(
            blikCode = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.BLIK_CODE_INVALID),
            ),
        )

        val actual = reducer.reduce(state, BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = false))

        assertTrue(actual.blikCode.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the field, then a visible error is hidden`() {
        val state = createInitialState().copy(
            blikCode = TextInputComponentState(
                error = TextInputComponentState.InputError(
                    CheckoutLocalizationKey.BLIK_CODE_INVALID,
                    isVisible = true,
                ),
            ),
            focusRequest = null,
        )

        val actual = reducer.reduce(state, BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = true))

        assertFalse(actual.blikCode.isErrorVisible)
    }

    @Test
    fun `when pay focuses the field, then the error it just revealed stays visible`() {
        val state = createInitialState().copy(
            blikCode = TextInputComponentState(
                error = TextInputComponentState.InputError(
                    CheckoutLocalizationKey.BLIK_CODE_INVALID,
                    isVisible = true,
                ),
            ),
            focusRequest = FocusRequest(BlikFormElementId.BLIK_CODE, keepErrorHighlight = true),
        )

        val actual = reducer.reduce(state, BlikIntent.UpdateFieldFocus(BlikFormElementId.BLIK_CODE, hasFocus = true))

        assertTrue(actual.blikCode.isErrorVisible)
        // The request has been answered, so it must not make the shopper's next tap look programmatic.
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when a focus request could not be fulfilled, then reporting it back clears it`() {
        val state = createInitialState().copy(focusRequest = FocusRequest(BlikFormElementId.BLIK_CODE))

        val actual = reducer.reduce(state, BlikIntent.FocusRequestConsumed(BlikFormElementId.BLIK_CODE))

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when intent is UpdateLoading, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.UpdateLoading(true))

        val expected = state.copy(isLoading = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `when pay is pressed and the blik code is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(
            blikCode = TextInputComponentState(
                text = "",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.BLIK_CODE_INVALID),
            ),
        )

        val actual = reducer.reduce(state, BlikIntent.HighlightValidationErrors)

        assertTrue(actual.blikCode.isErrorVisible)
        // keepErrorHighlight, so taking focus does not clear the error pay just revealed.
        assertEquals(FocusRequest(BlikFormElementId.BLIK_CODE, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the blik code is valid, then nothing is shown and no focus is asked for`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.HighlightValidationErrors)

        assertFalse(actual.blikCode.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    private fun createInitialState() = BlikComponentState(
        blikCode = TextInputComponentState(
            text = "",
            error = null,
        ),
        isLoading = false,
    )
}
