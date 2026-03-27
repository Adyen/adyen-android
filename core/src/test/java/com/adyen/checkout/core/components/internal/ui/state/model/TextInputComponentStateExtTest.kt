/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/3/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

internal class TextInputComponentStateExtTest {
    @Test
    fun `when field is hidden and empty, then value should be null`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertNull(paymentDataValue)
    }

    @Test
    fun `when field is hidden and has text, then value should be null`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "hidden",
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertNull(paymentDataValue)
    }

    @Test
    fun `when field is optional and empty, then value should be null`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertNull(paymentDataValue)
    }

    @Test
    fun `when field is optional and has text, then value should match content`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "optional",
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertEquals("optional", paymentDataValue)
    }

    @Test
    fun `when field is required and empty, then value should be null`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertNull(paymentDataValue)
    }

    @Test
    fun `when field is required and has text, then value should match content`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "text",
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val paymentDataValue = state.getPaymentDataValue()

        // THEN
        assertEquals("text", paymentDataValue)
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

    @Test
    fun `when field is hidden, then input should be valid`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertTrue(isAutomaticallyValid)
    }

    @Test
    fun `when field is hidden and has text, then input should be valid`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "hidden",
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertTrue(isAutomaticallyValid)
    }

    @Test
    fun `when field is optional and empty, then input should be valid`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertTrue(isAutomaticallyValid)
    }

    @Test
    fun `when field is optional and has text, then input should not be automatically valid`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "optional",
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertFalse(isAutomaticallyValid)
    }

    @Test
    fun `when field is required and empty, then input should not be automatically valid`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertFalse(isAutomaticallyValid)
    }

    @Test
    fun `when field is required and has text, then input should not be automatically valid`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "text",
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val isAutomaticallyValid = state.isNotRequiredAndValid()

        // THEN
        assertFalse(isAutomaticallyValid)
    }
}
