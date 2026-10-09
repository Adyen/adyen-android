/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Locale

internal class AddressComponentStateReducerTest {

    @Test
    fun `when the country changes then the other fields are kept`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("NL"))

        // THEN
        assertEquals("NL", result.country.selected)
        assertEquals(AddressSpec.DEFAULT, result.spec)
        assertEquals("Main Street 1", result.street.text)
        assertEquals("Apt 4", result.houseNumberOrName.text)
        assertEquals("94107", result.postalCode.text)
        assertEquals("San Francisco", result.city.text)
    }

    @Test
    fun `when the country changes then the fields follow the requirement policies of its spec`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("NL"))

        // THEN
        assertEquals(RequirementPolicy.Required, result.houseNumberOrName.requirementPolicy)
    }

    @Test
    fun `when the country changes then the states are loading`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("CA"))

        // THEN
        assertEquals(StateOrProvinceState.Loading(pendingCode = null), result.stateOrProvince)
    }

    @Test
    fun `when the same country is selected again then nothing changes`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("US"))

        // THEN
        assertSame(state, result)
    }

    @Test
    fun `when a field is updated then only that field changes`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCity("Oakland"))

        // THEN
        assertEquals(state.copy(city = state.city.updateText("Oakland")), result)
    }

    @Test
    fun `when the countries are loaded then they are filtered by the supported country codes`() {
        // GIVEN
        val params = createParams(supportedCountryCodes = setOf("NL", "US"))
        val state = createState(US_ADDRESS, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        val expected = listOf(
            AddressRegion(code = "NL", name = "Netherlands"),
            AddressRegion(code = "US", name = "United States"),
        )
        assertEquals(expected, result.countries)
        assertFalse(result.isLoadingCountries)
    }

    @Test
    fun `when the countries are loaded without the selected country then the selection is cleared`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(country = "XX"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertNull(result.country.selected)
        assertEquals(AddressSpec.DEFAULT, result.spec)
        assertEquals(StateOrProvinceState.Unavailable, result.stateOrProvince)
    }

    @Test
    fun `when the countries are loaded without a selected country then the fields are kept`() {
        // GIVEN
        val params = createParams(shopperLocale = Locale.ROOT)
        val state = createReducer(params).reduce(
            createState(address = null, params),
            AddressIntent.UpdateStreet("Main Street 1"),
        )

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertNull(result.country.selected)
        assertEquals("Main Street 1", result.street.text)
    }

    @Test
    fun `when loading the countries fails then the selected country is kept`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.failure(IOException())))

        // THEN
        assertEquals("US", result.country.selected)
        assertEquals(AddressSpec.US, result.spec)
        assertEquals(emptyList<AddressRegion>(), result.countries)
        assertFalse(result.isLoadingCountries)
    }

    @Test
    fun `when the states are loaded and empty then the state element is unavailable`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(country = "NL", stateOrProvince = null))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("NL", Result.success(emptyList())))

        // THEN
        assertEquals(StateOrProvinceState.Unavailable, result.stateOrProvince)
    }

    @Test
    fun `when the states are loaded with the pending state then it is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        val expected = StateOrProvinceState.Options(
            regions = US_STATES,
            picker = PickerInputComponentState(selected = "CA"),
        )
        assertEquals(expected, result.stateOrProvince)
    }

    @Test
    fun `when the states are loaded without the pending state then nothing is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(stateOrProvince = "TX"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        val expected = StateOrProvinceState.Options(
            regions = US_STATES,
            picker = PickerInputComponentState(selected = null),
        )
        assertEquals(expected, result.stateOrProvince)
    }

    @Test
    fun `when loading the states fails then the state element becomes free text with the pending state as text`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.failure(IOException())))

        // THEN
        val expected = StateOrProvinceState.FreeText(input = TextInputComponentState(text = "CA"))
        assertEquals(expected, result.stateOrProvince)
    }

    @Test
    fun `when a state is picked then its code is stored`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateStateOrProvince("NY"))

        // THEN
        val options = result.stateOrProvince as StateOrProvinceState.Options
        assertEquals("NY", options.picker.selected)
    }

    @Test
    fun `when the states could not be loaded then the typed state is stored`() {
        // GIVEN
        val state = createState(US_ADDRESS).copy(
            stateOrProvince = StateOrProvinceState.FreeText(input = TextInputComponentState()),
        )

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateStateOrProvince("California"))

        // THEN
        val freeText = result.stateOrProvince as StateOrProvinceState.FreeText
        assertEquals("California", freeText.input.text)
    }

    @Test
    fun `when states arrive for a country that is no longer selected then they are ignored`() {
        // GIVEN
        val state = createReducer().reduce(createState(US_ADDRESS), AddressIntent.UpdateCountry("CA"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertSame(state, result)
    }

    @Test
    fun `when prefilled without a country then the state element is unavailable`() {
        // GIVEN
        val params = createParams(shopperLocale = Locale.ROOT)
        val state = createState(US_ADDRESS, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.Prefill(address = null))

        // THEN
        assertNull(result.country.selected)
        assertEquals(StateOrProvinceState.Unavailable, result.stateOrProvince)
    }

    @Test
    fun `when prefilled then the whole state is replaced`() {
        // GIVEN
        val loadedCountries = createReducer().reduce(
            createState(address = null),
            AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)),
        )
        val editedState = createReducer().reduce(loadedCountries, AddressIntent.UpdateStreet("Unconfirmed edit"))

        // WHEN
        val result = createReducer().reduce(editedState, AddressIntent.Prefill(US_ADDRESS))

        // THEN
        val expected = createState(US_ADDRESS).copy(
            countries = loadedCountries.countries,
            isLoadingCountries = false,
        )
        assertEquals(expected, result)
    }

    private fun createParams(
        shopperLocale: Locale = Locale.US,
        supportedCountryCodes: Set<String> = emptySet(),
    ) = AddressComponentParams(
        shopperLocale = shopperLocale,
        supportedCountryCodes = supportedCountryCodes,
    )

    private fun createState(
        address: AddressModel?,
        params: AddressComponentParams = createParams(),
    ) = AddressComponentStateFactory(params).createState(address)

    private fun createReducer(
        params: AddressComponentParams = createParams(),
    ) = AddressComponentStateReducer(params, AddressComponentStateFactory(params))

    private fun AddressComponentState.withStates(regions: List<AddressRegion>) = copy(
        stateOrProvince = StateOrProvinceState.Options(
            regions = regions,
            picker = PickerInputComponentState(selected = "CA"),
        ),
    )

    companion object {
        private val US_ADDRESS = AddressModel(
            country = "US",
            street = "Main Street 1",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            city = "San Francisco",
            stateOrProvince = "CA",
        )

        private val COUNTRY_ITEMS = listOf(
            AddressItem(id = "DE", name = "Germany"),
            AddressItem(id = "NL", name = "Netherlands"),
            AddressItem(id = "US", name = "United States"),
        )

        private val US_STATE_ITEMS = listOf(
            AddressItem(id = "CA", name = "California"),
            AddressItem(id = "NY", name = "New York"),
        )

        private val US_STATES = listOf(
            AddressRegion(code = "CA", name = "California"),
            AddressRegion(code = "NY", name = "New York"),
        )
    }
}
