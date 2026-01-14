/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `when intent is UpdateBlikCodeFocus, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.UpdateBlikCodeFocus(true))

        val expected = state.copy(blikCode = state.blikCode.updateFocus(true))
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is UpdateLoading, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.UpdateLoading(true))

        val expected = state.copy(isLoading = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is HighlightValidationErrors and blik code has error, then state is updated`() {
        val state = createInitialState().copy(
            blikCode = TextInputComponentState(
                text = "",
                isFocused = false,
                errorMessage = CheckoutLocalizationKey.BLIK_CODE_INVALID,
                showError = false,
            ),
        )

        val actual = reducer.reduce(state, BlikIntent.HighlightValidationErrors)

        assertTrue(actual.blikCode.showError)
        assertTrue(actual.blikCode.isFocused)
    }

    @Test
    fun `when intent is HighlightValidationErrors and blik code has no error, then state is not updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.HighlightValidationErrors)

        assertFalse(actual.blikCode.showError)
        assertFalse(actual.blikCode.isFocused)
    }

    private fun createInitialState() = BlikComponentState(
        blikCode = TextInputComponentState(
            text = "",
            isFocused = false,
            errorMessage = null,
            showError = false,
        ),
        isLoading = false,
    )
}
