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
import org.junit.jupiter.api.assertNull

/**
 * Tests the extensions in `TextInputComponentStateExt.kt`: how the requirement policy of a field affects its payment
 * data value and whether it needs validation.
 */
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
    fun `when field is hidden, then input should not require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertFalse(requiresValidation)
    }

    @Test
    fun `when field is hidden and has text, then input should not require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "hidden",
            requirementPolicy = RequirementPolicy.Hidden,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertFalse(requiresValidation)
    }

    @Test
    fun `when field is optional and empty, then input should not require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertFalse(requiresValidation)
    }

    @Test
    fun `when field is optional and has text, then input should require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "optional",
            requirementPolicy = RequirementPolicy.Optional,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertTrue(requiresValidation)
    }

    @Test
    fun `when field is required and empty, then input should require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertTrue(requiresValidation)
    }

    @Test
    fun `when field is required and has text, then input should require validation`() {
        // GIVEN
        val state = TextInputComponentState(
            text = "text",
            requirementPolicy = RequirementPolicy.Required,
        )

        // WHEN
        val requiresValidation = state.requiresValidation()

        // THEN
        assertTrue(requiresValidation)
    }
}
