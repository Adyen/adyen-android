/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `when intent is UpdateLoading, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, BlikIntent.UpdateLoading(true))

        val expected = state.copy(isLoading = true)
        assertEquals(expected, actual)
    }

    private fun createInitialState() = BlikComponentState(
        blikCode = TextInputComponentState(
            text = "",
            error = null,
        ),
        isLoading = false,
    )
}
