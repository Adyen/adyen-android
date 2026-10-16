/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
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
import java.util.Locale

internal class AddressViewStateProducerTest {

    private val producer = AddressViewStateProducer(shopperLocale = Locale.US)

    @Test
    fun `when the country is US then the elements are produced in the US order`() {
        // GIVEN
        val state = createState()

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val expected = listOf(
            AddressFormElementId.COUNTRY,
            AddressFormElementId.STREET,
            AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            AddressFormElementId.CITY,
            AddressFormElementId.STATE_OR_PROVINCE,
            AddressFormElementId.POSTAL_CODE,
        )
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
        val state = createState().copy(
            stateOrProvince = StateOrProvinceState.Options(
                regions = US_STATES,
                picker = PickerInputComponentState(error = visibleError),
            ),
        )

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
    fun `when the countries are loaded then the selected country has the loaded name`() {
        // GIVEN
        val state = createState().copy(countries = listOf(AddressRegion(code = "US", name = "USA")))

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val country = viewState.elements.filterIsInstance<AddressFormElement.Country>().single()
        assertEquals(AddressRegion(code = "US", name = "USA"), country.selected)
    }

    @Test
    fun `when the country list is unavailable then the selected country falls back to the platform name`() {
        // GIVEN
        val state = createState().copy(countries = emptyList())

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val country = viewState.elements.filterIsInstance<AddressFormElement.Country>().single()
        assertEquals(AddressRegion(code = "US", name = "United States"), country.selected)
    }

    @Test
    fun `when the country is US then the postal code element carries a maximum of ten`() {
        // GIVEN
        val state = createState()

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val postalCode = viewState.elements.filterIsInstance<AddressFormElement.PostalCode>().single()
        assertEquals(10, postalCode.maxLength)
        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL_ZIP_CODE, postalCode.label)
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

    @ParameterizedTest
    @MethodSource("statesWithoutOptionsSource")
    fun `when the states have no options then there is no state element or state picker`(
        stateOrProvince: StateOrProvinceState,
    ) {
        // GIVEN
        val state = createState().copy(stateOrProvince = stateOrProvince)

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        assertTrue(viewState.elements.none { it is AddressFormElement.StateOrProvince })
        assertNull(viewState.statePickerViewState)
    }

    @Test
    fun `when the house number is produced then it carries its description`() {
        // GIVEN
        val state = createState()

        // WHEN
        val viewState = producer.produce(state)

        // THEN
        val houseNumber = viewState.elements.filterIsInstance<AddressFormElement.HouseNumberOrName>().single()
        val description = houseNumber.textInputViewState.supportingText
        assertEquals(CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_DESCRIPTION, description)
    }

    /** A US form whose states loaded, with nothing typed yet. */
    private fun createState(selectedState: String? = null) = AddressComponentState(
        spec = AddressSpec.US,
        countries = COUNTRIES,
        isLoadingCountries = false,
        country = PickerInputComponentState(selected = "US"),
        street = TextInputComponentState(),
        houseNumberOrName = TextInputComponentState(requirementPolicy = RequirementPolicy.Optional),
        postalCode = TextInputComponentState(),
        city = TextInputComponentState(),
        stateOrProvince = StateOrProvinceState.Options(
            regions = US_STATES,
            picker = PickerInputComponentState(selected = selectedState),
        ),
    )

    companion object {
        private val COUNTRIES = listOf(
            AddressRegion(code = "NL", name = "Netherlands"),
            AddressRegion(code = "US", name = "United States"),
        )

        private val CALIFORNIA = AddressRegion(code = "CA", name = "California")

        private val US_STATES = listOf(CALIFORNIA, AddressRegion(code = "NY", name = "New York"))

        @JvmStatic
        fun statesWithoutOptionsSource() = listOf(
            arguments(StateOrProvinceState.Loading(pendingCode = null)),
            arguments(StateOrProvinceState.Unavailable),
        )
    }
}
