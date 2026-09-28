/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
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
internal class MBWayComponentStatePostProcessorTest {

    private val postProcessor = MBWayComponentStatePostProcessor()

    /**
     * The country picker comes first in the form but cannot take focus, so the opening request skips it.
     */
    @Test
    fun `when the initial state is created, then the phone number is asked for focus`() {
        val state = createInitialState().copy(phoneNumber = invalidField())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(MBWayFormElementId.PHONE_NUMBER), actual.focusRequest)
    }

    @Test
    fun `when the phone number loses focus, then an error it was holding back is shown`() {
        val state = createInitialState().copy(phoneNumber = invalidField())

        val actual = postProcessor.process(
            state,
            MBWayIntent.UpdateFieldFocus(MBWayFormElementId.PHONE_NUMBER, hasFocus = false),
        )

        assertTrue(actual.phoneNumber.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the phone number, then a visible error is hidden`() {
        val state = createInitialState().copy(phoneNumber = invalidField(isErrorVisible = true))

        val actual = postProcessor.process(
            state,
            MBWayIntent.UpdateFieldFocus(MBWayFormElementId.PHONE_NUMBER, hasFocus = true),
        )

        assertFalse(actual.phoneNumber.isErrorVisible)
    }

    @Test
    fun `when pay is pressed and the phone number is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(phoneNumber = invalidField())

        val actual = postProcessor.process(state, MBWayIntent.HighlightValidationErrors)

        assertTrue(actual.phoneNumber.isErrorVisible)
        assertEquals(FocusRequest(MBWayFormElementId.PHONE_NUMBER, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the phone number is valid, then nothing shows and no focus is asked for`() {
        val state = createInitialState()

        val actual = postProcessor.process(state, MBWayIntent.HighlightValidationErrors)

        assertFalse(actual.phoneNumber.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the country picker cannot be focused, then asking to focus it changes nothing`() {
        val state = createInitialState()

        val actual = postProcessor.process(
            state,
            MBWayIntent.UpdateFieldFocus(MBWayFormElementId.COUNTRY_CODE, hasFocus = true),
        )

        assertEquals(state, actual)
    }

    @Test
    fun `when a focus request could not be fulfilled, then reporting it back clears it`() {
        val state = createInitialState().copy(focusRequest = FocusRequest(MBWayFormElementId.PHONE_NUMBER))

        val actual = postProcessor.process(state, MBWayIntent.FocusRequestConsumed(MBWayFormElementId.PHONE_NUMBER))

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the intent decides no focus, then the state is untouched`() {
        val state = createInitialState().copy(phoneNumber = invalidField())

        val actual = postProcessor.process(state, MBWayIntent.UpdateLoading(true))

        assertEquals(state, actual)
    }

    private fun invalidField(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isErrorVisible),
    )

    private fun createInitialState() = MBWayComponentState(
        countries = emptyList(),
        selectedCountryCode = CountryModel("NL", "Netherlands", "+31"),
        phoneNumber = TextInputComponentState(),
        isLoading = false,
    )
}
