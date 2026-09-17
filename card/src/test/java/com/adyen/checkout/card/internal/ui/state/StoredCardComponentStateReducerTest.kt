/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StoredCardComponentStateReducerTest {

    private lateinit var reducer: StoredCardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = StoredCardComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateLoading with true, then isLoading is set to true`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is UpdateLoading with false, then isLoading is set to false`() {
        val state = createInitialState().copy(isLoading = true)

        val actual = reducer.reduce(state, StoredCardIntent.UpdateLoading(false))

        assertFalse(actual.isLoading)
    }

    private fun createInitialState() = StoredCardComponentState(
        securityCode = TextInputComponentState(),
        isLoading = false,
        detectedCardType = null,
    )
}
