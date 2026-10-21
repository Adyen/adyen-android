/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AddressViewStateProducerTest {

    private val producer = AddressViewStateProducer()

    @ParameterizedTest
    @MethodSource("countryOrderSource")
    fun `when a country is selected then the elements are produced in its order`(
        countryCode: String,
        expected: List<AddressFormElementId>,
    ) {
        // GIVEN
        val state = createState(countryCode = countryCode)

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        assertEquals(expected, viewState.elements.map { it.id })
    }

    @Test
    fun `when a field has a visible error then its view state is an error`() {
        // GIVEN
        val visibleError = InputError(CheckoutLocalizationKey.ADDRESS_CITY_ERROR, isVisible = true)
        val state = createState().copy(city = TextInputComponentState(error = visibleError))

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val city = viewState.elements.filterIsInstance<AddressFormElement.City>().single()
        assertTrue(city.textInputViewState.isError)
        assertEquals(CheckoutLocalizationKey.ADDRESS_CITY_ERROR, city.textInputViewState.supportingText)
    }

    @Test
    fun `when a field is optional then its view state is optional`() {
        // GIVEN
        val state = createState()

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val houseNumber = viewState.elements.filterIsInstance<AddressFormElement.HouseNumberOrName>().single()
        val street = viewState.elements.filterIsInstance<AddressFormElement.Street>().single()
        assertTrue(houseNumber.textInputViewState.isOptional)
        assertFalse(street.textInputViewState.isOptional)
    }

    @Test
    fun `when the state has options then a picker element is produced`() {
        // GIVEN
        val state = createState(selectedState = "CA")

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = AddressFormElement.StateOrProvince(
            label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            selected = CALIFORNIA,
            errorMessage = null,
        )
        assertEquals(expected, viewState.elements.filterIsInstance<AddressFormElement.StateOrProvince>().single())
    }

    @Test
    fun `when a picker has a visible error then its element carries the message`() {
        // GIVEN
        val visibleError = InputError(CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR, isVisible = true)
        val state = createState().copy(stateOrProvince = PickerInputComponentState(error = visibleError))

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val stateOrProvince = viewState.elements.filterIsInstance<AddressFormElement.StateOrProvince>().single()
        assertEquals(CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR, stateOrProvince.errorMessage)
    }

    @Test
    fun `when a picker error is not visible yet then its element has no message`() {
        // GIVEN
        val state = createState().copy(
            country = PickerInputComponentState(error = InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR)),
        )

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val country = viewState.elements.filterIsInstance<AddressFormElement.Country>().single()
        assertNull(country.errorMessage)
    }

    @Test
    fun `when the countries are loaded then the country element has its label and the loaded name`() {
        // GIVEN
        val state = createState().copy(countries = listOf(AddressRegion(code = "US", name = "USA")))

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = AddressFormElement.Country(
            label = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
            selected = AddressRegion(code = "US", name = "USA"),
            errorMessage = null,
        )
        assertEquals(expected, viewState.elements.filterIsInstance<AddressFormElement.Country>().single())
    }

    @Test
    fun `when the country is NL then the postal code element carries the NL maximum`() {
        // GIVEN
        val state = createState(countryCode = "NL")

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val postalCode = viewState.elements.filterIsInstance<AddressFormElement.PostalCode>().single()
        assertEquals(7, postalCode.maxLength)
    }

    @Test
    fun `when the countries are loaded then the country picker view state lists them with the selection`() {
        // GIVEN
        val state = createState()

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = PickerViewState(
            title = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
            regions = COUNTRIES,
            selected = AddressRegion(code = "US", name = "United States"),
        )
        assertEquals(expected, viewState.countryPickerViewState)
    }

    @Test
    fun `when the state is a picker then its picker view state is titled with the state label`() {
        // GIVEN
        val state = createState(selectedState = "CA")

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = PickerViewState(
            title = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            regions = US_STATES,
            selected = CALIFORNIA,
        )
        assertEquals(expected, viewState.statePickerViewState)
    }

    @Test
    fun `when the states are loading then an empty state element is produced`() {
        // GIVEN
        val state = createState().copy(states = emptyList())

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = AddressFormElement.StateOrProvince(
            label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            selected = null,
            errorMessage = null,
        )
        assertEquals(expected, viewState.elements.filterIsInstance<AddressFormElement.StateOrProvince>().single())
    }

    @Test
    fun `when the states are loading then the state picker is empty`() {
        // GIVEN
        val state = createState().copy(states = emptyList())

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = PickerViewState(
            title = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
            regions = emptyList(),
            selected = null,
        )
        assertEquals(expected, viewState.statePickerViewState)
    }

    @Test
    fun `when the country has no states then there is no state element or state picker`() {
        // GIVEN
        val state = createState(countryCode = "NL")

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        assertTrue(viewState.elements.none { it is AddressFormElement.StateOrProvince })
        assertNull(viewState.statePickerViewState)
    }

    /** A form for [countryCode], US unless given, whose states loaded, with nothing typed yet. */
    private fun createState(countryCode: String = "US", selectedState: String? = null): AddressComponentState {
        val spec = AddressSpec.fromCountryCode(countryCode)
        return AddressComponentState(
            countries = COUNTRIES,
            country = PickerInputComponentState(selected = countryCode),
            street = TextInputComponentState(),
            houseNumberOrName = TextInputComponentState(
                requirementPolicy = spec.getFieldSpec(AddressFormElementId.HOUSE_NUMBER_OR_NAME).requirementPolicy,
            ),
            postalCode = TextInputComponentState(),
            city = TextInputComponentState(),
            states = if (spec.hasStates) US_STATES else emptyList(),
            stateOrProvince = PickerInputComponentState(selected = selectedState),
        )
    }

    companion object {
        private val COUNTRIES = listOf(
            AddressRegion(code = "BR", name = "Brazil"),
            AddressRegion(code = "CA", name = "Canada"),
            AddressRegion(code = "GB", name = "United Kingdom"),
            AddressRegion(code = "NL", name = "Netherlands"),
            AddressRegion(code = "US", name = "United States"),
        )

        private val CALIFORNIA = AddressRegion(code = "CA", name = "California")

        private val US_STATES = listOf(CALIFORNIA, AddressRegion(code = "NY", name = "New York"))

        @JvmStatic
        fun countryOrderSource() = listOf(
            arguments(
                "BR",
                listOf(
                    AddressFormElementId.COUNTRY,
                    AddressFormElementId.STREET,
                    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                    AddressFormElementId.POSTAL_CODE,
                    AddressFormElementId.CITY,
                    AddressFormElementId.STATE_OR_PROVINCE,
                ),
            ),
            arguments(
                "CA",
                listOf(
                    AddressFormElementId.COUNTRY,
                    AddressFormElementId.STREET,
                    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                    AddressFormElementId.CITY,
                    AddressFormElementId.POSTAL_CODE,
                    AddressFormElementId.STATE_OR_PROVINCE,
                ),
            ),
            arguments(
                "GB",
                listOf(
                    AddressFormElementId.COUNTRY,
                    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                    AddressFormElementId.STREET,
                    AddressFormElementId.CITY,
                    AddressFormElementId.POSTAL_CODE,
                ),
            ),
            arguments(
                "US",
                listOf(
                    AddressFormElementId.COUNTRY,
                    AddressFormElementId.STREET,
                    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                    AddressFormElementId.CITY,
                    AddressFormElementId.STATE_OR_PROVINCE,
                    AddressFormElementId.POSTAL_CODE,
                ),
            ),
            arguments(
                "NL",
                listOf(
                    AddressFormElementId.COUNTRY,
                    AddressFormElementId.STREET,
                    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                    AddressFormElementId.POSTAL_CODE,
                    AddressFormElementId.CITY,
                ),
            ),
        )
    }
}
