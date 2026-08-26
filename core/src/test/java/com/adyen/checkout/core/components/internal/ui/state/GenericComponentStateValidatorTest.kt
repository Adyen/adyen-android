/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GenericComponentStateValidatorTest {

    private lateinit var validator: GenericComponentStateValidator

    @BeforeEach
    fun beforeEach() {
        validator = GenericComponentStateValidator()
    }

    @Test
    fun `when validating a state, then the state is returned unchanged`() {
        // GIVEN
        val state = GenericComponentState(isLoading = true)

        // WHEN
        val actual = validator.validate(state)

        // THEN
        assertEquals(state, actual)
    }

    @Test
    fun `when checking a state, then it is always valid`() {
        // GIVEN
        val state = GenericComponentState(isLoading = false)

        // WHEN
        val actual = validator.isValid(state)

        // THEN
        assertTrue(actual)
    }
}
