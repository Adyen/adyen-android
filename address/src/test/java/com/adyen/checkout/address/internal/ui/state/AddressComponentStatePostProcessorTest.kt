/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError
import com.adyen.checkout.core.components.internal.ui.state.model.isErrorVisible
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The post processor runs after the validator, so every state here is written as it would be once validation has
 * run: a field that should count as unfilled carries an error.
 */
internal class AddressComponentStatePostProcessorTest {

    private val postProcessor = AddressComponentStatePostProcessor()

    /**
     * The country picker comes first in the form but cannot take focus, so the request skips it. In the US layout the
     * city comes before the postal code.
     */
    @Test
    fun `when the initial state is processed then focus goes to the first invalid text input`() {
        // GIVEN
        val state = createState().copy(
            country = invalidPicker(),
            city = invalidField(),
            postalCode = invalidField(),
        )

        // WHEN
        val result = postProcessor.processInitialState(state)

        // THEN
        assertEquals(FocusRequest(AddressFormElementId.CITY), result.focusRequest)
    }

    @Test
    fun `when a country is picked then focus goes to the first invalid text input after the country`() {
        // GIVEN
        val state = createState().copy(
            city = invalidField(),
            focusRequest = FocusRequest(AddressFormElementId.POSTAL_CODE, showErrorIfPresent = true),
        )

        // WHEN
        val result = process(state, AddressIntent.UpdateCountry("US"))

        // THEN
        assertEquals(FocusRequest(AddressFormElementId.CITY), result.focusRequest)
    }

    /**
     * A prefill replaces the whole state, including its focus request, every time the form is shown. Without a new
     * request the form would open without focus.
     */
    @Test
    fun `when the form is prefilled then focus goes to the first invalid text input`() {
        // GIVEN
        val state = createState().copy(city = invalidField())

        // WHEN
        val result = process(state, AddressIntent.Prefill(address = null))

        // THEN
        assertEquals(FocusRequest(AddressFormElementId.CITY), result.focusRequest)
    }

    @Test
    fun `when validation errors are highlighted then every error becomes visible`() {
        // GIVEN
        val state = createState().copy(
            country = invalidPicker(),
            street = invalidField(),
            houseNumberOrName = invalidField(),
            city = invalidField(),
            stateOrProvince = StateOrProvinceState.Options(regions = STATES, picker = invalidPicker()),
            postalCode = invalidField(),
        )

        // WHEN
        val result = process(state, AddressIntent.HighlightValidationErrors)

        // THEN
        val options = result.stateOrProvince as StateOrProvinceState.Options
        assertTrue(result.country.error?.isVisible == true)
        assertTrue(result.street.isErrorVisible)
        assertTrue(result.houseNumberOrName.isErrorVisible)
        assertTrue(result.city.isErrorVisible)
        assertTrue(options.picker.error?.isVisible == true)
        assertTrue(result.postalCode.isErrorVisible)
        assertEquals(FocusRequest(AddressFormElementId.STREET, showErrorIfPresent = true), result.focusRequest)
    }

    @Test
    fun `when validation errors are highlighted then the free text state error becomes visible`() {
        // GIVEN
        val state = createState().copy(stateOrProvince = StateOrProvinceState.FreeText(input = invalidField()))

        // WHEN
        val result = process(state, AddressIntent.HighlightValidationErrors)

        // THEN
        val freeText = result.stateOrProvince as StateOrProvinceState.FreeText
        assertTrue(freeText.input.isErrorVisible)
        assertEquals(
            FocusRequest(AddressFormElementId.STATE_OR_PROVINCE_TEXT, showErrorIfPresent = true),
            result.focusRequest,
        )
    }

    @Test
    fun `when a field gains focus then its error is hidden`() {
        // GIVEN
        val state = createState().copy(city = invalidField(isErrorVisible = true))

        // WHEN
        val result = process(state, AddressIntent.UpdateFieldFocus(AddressFormElementId.CITY, hasFocus = true))

        // THEN
        assertFalse(result.city.isErrorVisible)
    }

    @Test
    fun `when a field loses focus then its error is shown`() {
        // GIVEN
        val state = createState().copy(city = invalidField())

        // WHEN
        val result = process(state, AddressIntent.UpdateFieldFocus(AddressFormElementId.CITY, hasFocus = false))

        // THEN
        assertTrue(result.city.isErrorVisible)
    }

    @Test
    fun `when the focus request is consumed then it is cleared`() {
        // GIVEN
        val state = createState().copy(
            city = invalidField(),
            focusRequest = FocusRequest(AddressFormElementId.CITY),
        )

        // WHEN
        val result = process(state, AddressIntent.FocusRequestConsumed(AddressFormElementId.CITY))

        // THEN
        assertNull(result.focusRequest)
    }

    private fun process(state: AddressComponentState, intent: AddressIntent) =
        postProcessor.process(state, state, intent)

    private fun invalidField(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = InputError(ERROR_MESSAGE, isErrorVisible),
    )

    private fun invalidPicker() = PickerInputComponentState<String>(error = InputError(ERROR_MESSAGE))

    /** A valid US form: every element is filled, and the states loaded with one selected. */
    private fun createState() = AddressComponentState(
        spec = AddressSpec.US,
        countries = emptyList(),
        isLoadingCountries = false,
        country = PickerInputComponentState(selected = "US"),
        street = TextInputComponentState(text = "Main Street 1"),
        houseNumberOrName = TextInputComponentState(requirementPolicy = RequirementPolicy.Optional),
        postalCode = TextInputComponentState(text = "94107"),
        city = TextInputComponentState(text = "San Francisco"),
        stateOrProvince = StateOrProvinceState.Options(
            regions = STATES,
            picker = PickerInputComponentState(selected = "CA"),
        ),
    )

    companion object {
        // Any key works: only the presence of an error matters here.
        private val ERROR_MESSAGE = CheckoutLocalizationKey.ADDRESS_STREET_LABEL

        private val STATES = listOf(AddressRegion(code = "CA", name = "California"))
    }
}
