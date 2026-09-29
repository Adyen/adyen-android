/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
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
     * The country picker comes first in the form but cannot take focus, so the opening request skips it.
     */
    @Test
    fun `when the initial state is created, then the postal code is asked for focus`() {
        val state = createState(postalCode = invalidPostalCode())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(AddressFormElementId.POSTAL_CODE), actual.focusRequest)
    }

    @Test
    fun `when the initial state is already valid, then no focus is asked for`() {
        val state = createState()

        val actual = postProcessor.processInitialState(state)

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the postal code loses focus, then an error it was holding back is shown`() {
        val state = createState(postalCode = invalidPostalCode())

        val actual = process(state, AddressIntent.UpdateFieldFocus(AddressFormElementId.POSTAL_CODE, hasFocus = false))

        assertTrue(actual.postalCode.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the postal code, then a visible error is hidden`() {
        val state = createState(postalCode = invalidPostalCode(isErrorVisible = true))

        val actual = process(state, AddressIntent.UpdateFieldFocus(AddressFormElementId.POSTAL_CODE, hasFocus = true))

        assertFalse(actual.postalCode.isErrorVisible)
    }

    @Test
    fun `when confirm is pressed and every field is invalid, then all errors show and the postal code gets focus`() {
        val state = createState(country = invalidCountry(), postalCode = invalidPostalCode())

        val actual = process(state, AddressIntent.HighlightValidationErrors)

        assertEquals(true, actual.country.error?.isVisible)
        assertTrue(actual.postalCode.isErrorVisible)
        assertEquals(
            FocusRequest(AddressFormElementId.POSTAL_CODE, showErrorIfPresent = true),
            actual.focusRequest,
        )
    }

    @Test
    fun `when confirm is pressed and only the country is invalid, then its error shows and no focus is asked for`() {
        val state = createState(country = invalidCountry())

        val actual = process(state, AddressIntent.HighlightValidationErrors)

        assertEquals(true, actual.country.error?.isVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when a country is picked and the postal code is not filled in, then the postal code is asked for focus`() {
        val state = createState(postalCode = invalidPostalCode())

        val actual = process(state, AddressIntent.UpdateCountry(NETHERLANDS))

        assertEquals(FocusRequest(AddressFormElementId.POSTAL_CODE), actual.focusRequest)
    }

    @Test
    fun `when a country is picked and the postal code is filled in, then no focus is asked for`() {
        val state = createState()

        val actual = process(state, AddressIntent.UpdateCountry(NETHERLANDS))

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the form is prefilled with an incomplete address, then the postal code is asked for focus`() {
        val state = createState(postalCode = invalidPostalCode())

        val actual = process(state, AddressIntent.Prefill(AddressModel(country = "NL")))

        assertEquals(FocusRequest(AddressFormElementId.POSTAL_CODE), actual.focusRequest)
    }

    @Test
    fun `when a focus request is reported back, then it is cleared`() {
        val state = createState().copy(focusRequest = FocusRequest(AddressFormElementId.POSTAL_CODE))

        val actual = process(state, AddressIntent.FocusRequestConsumed(AddressFormElementId.POSTAL_CODE))

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the postal code is typed, then the state is untouched`() {
        val state = createState(postalCode = invalidPostalCode())

        val actual = process(state, AddressIntent.UpdatePostalCode("1"))

        assertEquals(state, actual)
    }

    private fun process(state: AddressComponentState, intent: AddressIntent) =
        postProcessor.process(state, state, intent)

    private fun invalidPostalCode(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, isErrorVisible),
    )

    private fun invalidCountry() = CountryInputState(
        selectedCountry = null,
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR),
    )

    private fun createState(
        country: CountryInputState = CountryInputState(selectedCountry = NETHERLANDS),
        postalCode: TextInputComponentState = TextInputComponentState(text = "1234 AB"),
    ) = AddressComponentState(
        countries = listOf(NETHERLANDS),
        country = country,
        postalCode = postalCode,
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
    }
}
