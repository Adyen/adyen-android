/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GenericComponentStateReducerTest {

    private lateinit var reducer: GenericComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = GenericComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateLoading with true, then state is loading`() {
        // GIVEN
        val state = GenericComponentState(isLoading = false)

        // WHEN
        val actual = reducer.reduce(state, GenericIntent.UpdateLoading(true))

        // THEN
        assertEquals(state.copy(isLoading = true), actual)
    }

    @Test
    fun `when intent is UpdateLoading with false, then state is not loading`() {
        // GIVEN
        val state = GenericComponentState(isLoading = true)

        // WHEN
        val actual = reducer.reduce(state, GenericIntent.UpdateLoading(false))

        // THEN
        assertEquals(state.copy(isLoading = false), actual)
    }
}
