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

internal class GenericComponentStateFactoryTest {

    private lateinit var factory: GenericComponentStateFactory

    @BeforeEach
    fun beforeEach() {
        factory = GenericComponentStateFactory()
    }

    @Test
    fun `when creating initial state, then conform with expected state`() {
        // WHEN
        val actual = factory.createInitialState()

        // THEN
        val expected = GenericComponentState(isLoading = false)
        assertEquals(expected, actual)
    }
}
