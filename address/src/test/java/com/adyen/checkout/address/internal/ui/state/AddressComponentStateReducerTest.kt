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
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `when the country changes then the state is reset`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("CA"))

        // THEN
        assertEquals(emptyList<AddressRegion>(), result.states)
        assertEquals(PickerInputComponentState<String>(), result.stateOrProvince)
    }

    @Test
    fun `when a country that is not loaded is picked then it is ignored`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateCountry("DE"))

        // THEN
        assertSame(state, result)
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
    }

    @Test
    fun `when the countries are loaded then the shopper locale country is selected`() {
        // GIVEN
        val state = createState(address = null, countries = emptyList())

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("US", result.country.selected)
        assertEquals(AddressSpec.US, result.spec)
    }

    @Test
    fun `when the countries are loaded without the shopper locale country then no country is selected`() {
        // GIVEN
        val params = createParams(shopperLocale = Locale.FRANCE)
        val state = createState(address = null, params, countries = emptyList())

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertNull(result.country.selected)
        assertEquals(AddressSpec.DEFAULT, result.spec)
    }

    @Test
    fun `when the countries are loaded then the typed fields are kept`() {
        // GIVEN
        val state = createReducer().reduce(
            createState(address = null, countries = emptyList()),
            AddressIntent.UpdateStreet("Main Street 1"),
        )

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("US", result.country.selected)
        assertEquals("Main Street 1", result.street.text)
    }

    @Test
    fun `when the countries are loaded then a selected country they contain is kept`() {
        // GIVEN
        val state = createReducer().reduce(createState(US_ADDRESS), AddressIntent.UpdateCountry("NL"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("NL", result.country.selected)
    }

    @Test
    fun `when the countries fail to load after a country was selected then no country is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.failure(IOException())))

        // THEN
        assertNull(result.country.selected)
        assertEquals(AddressSpec.DEFAULT, result.spec)
    }

    @Test
    fun `when loading the countries fails then no country is selected`() {
        // GIVEN
        val state = createState(address = null, countries = emptyList())

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.failure(IOException())))

        // THEN
        assertNull(result.country.selected)
        assertEquals(emptyList<AddressRegion>(), result.countries)
    }

    @Test
    fun `when the states are loaded with the selected state then it stays selected`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals(US_STATES, result.states)
        assertEquals("CA", result.stateOrProvince.selected)
    }

    @Test
    fun `when the states are loaded without the selected state then the selection is cleared`() {
        // GIVEN
        val texas = AddressRegion(code = "TX", name = "Texas")
        val state = createState(US_ADDRESS).copy(
            states = listOf(texas),
            stateOrProvince = PickerInputComponentState(selected = texas.code),
        )

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals(US_STATES, result.states)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when loading the states fails then the selected state is cleared`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.failure(IOException())))

        // THEN
        assertEquals(emptyList<AddressRegion>(), result.states)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when a state is picked then its code is stored`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateStateOrProvince("NY"))

        // THEN
        assertEquals("NY", result.stateOrProvince.selected)
    }

    @Test
    fun `when a state that is not loaded is picked then it is ignored`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.UpdateStateOrProvince("TX"))

        // THEN
        assertSame(state, result)
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
    fun `when reset without a country then no state is selected`() {
        // GIVEN
        val params = createParams(shopperLocale = Locale.ROOT)
        val state = createState(US_ADDRESS, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.Reset(address = null))

        // THEN
        assertNull(result.country.selected)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when reset then the whole state is replaced`() {
        // GIVEN
        val loadedCountries = createReducer().reduce(
            createState(address = null),
            AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)),
        )
        val editedState = createReducer().reduce(loadedCountries, AddressIntent.UpdateStreet("Unconfirmed edit"))

        // WHEN
        val result = createReducer().reduce(editedState, AddressIntent.Reset(US_ADDRESS))

        // THEN
        val expected = createState(US_ADDRESS, countries = loadedCountries.countries)
        assertEquals(expected, result)
    }

    /** Otherwise the state row would show its code instead of its name until the states are fetched again. */
    @Test
    fun `when reset with the same country then the loaded states are kept`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.Reset(US_ADDRESS.copy(stateOrProvince = "NY")))

        // THEN
        assertEquals(US_STATES, result.states)
        assertEquals("NY", result.stateOrProvince.selected)
    }

    @Test
    fun `when reset with the same country and a state the loaded states lack then no state is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.Reset(US_ADDRESS.copy(stateOrProvince = "TX")))

        // THEN
        assertEquals(US_STATES, result.states)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when reset with another country then the loaded states are dropped`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.Reset(US_ADDRESS.copy(country = "CA")))

        // THEN
        assertEquals(emptyList<AddressRegion>(), result.states)
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
        countries: List<AddressRegion> = LOADED_COUNTRIES,
    ) = AddressComponentStateFactory(params).createState(address, countries)

    private fun createReducer(
        params: AddressComponentParams = createParams(),
    ) = AddressComponentStateReducer(params, AddressComponentStateFactory(params))

    private fun AddressComponentState.withStates(regions: List<AddressRegion>) = copy(
        states = regions,
        stateOrProvince = PickerInputComponentState(selected = "CA"),
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

        private val LOADED_COUNTRIES = listOf(
            AddressRegion(code = "CA", name = "Canada"),
            AddressRegion(code = "NL", name = "Netherlands"),
            AddressRegion(code = "US", name = "United States"),
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
