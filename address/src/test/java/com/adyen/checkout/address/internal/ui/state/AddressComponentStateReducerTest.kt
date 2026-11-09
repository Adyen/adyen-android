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
import com.adyen.checkout.core.components.internal.ui.state.model.PickerInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.io.IOException

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
    fun `when the countries are loaded then they keep the order of the backend`() {
        // GIVEN
        val items = listOf(
            AddressItem(id = "US", name = "United States"),
            AddressItem(id = "DE", name = "Germany"),
            AddressItem(id = "NL", name = "Netherlands"),
        )

        // WHEN
        val result = createReducer().reduce(
            createState(address = null),
            AddressIntent.CountriesLoaded(Result.success(items)),
        )

        // THEN
        assertEquals(listOf("US", "DE", "NL"), result.countries.map { it.code })
    }

    @Test
    fun `when the countries are loaded without a selected country then the first country is selected`() {
        // GIVEN
        val state = createState(address = null)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("DE", result.country.selected)
    }

    /** The order of the backend decides, not the order of the supported country codes. */
    @Test
    fun `when the countries are loaded with supported country codes then the first supported country is selected`() {
        // GIVEN
        val params = createParams(supportedCountryCodes = linkedSetOf("US", "NL"))
        val state = createState(address = null, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("NL", result.country.selected)
    }

    /** The countries are requested again on every show, so a reload must not drop the loaded states. */
    @Test
    fun `when the countries are loaded with the selected country then it stays selected with its states`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("US", result.country.selected)
        assertEquals(US_STATES, result.states)
        assertEquals("CA", result.stateOrProvince.selected)
    }

    @Test
    fun `when the loaded countries leave out the selected country then the first country is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(country = "XX"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("DE", result.country.selected)
        assertEquals(AddressSpec.DEFAULT, result.spec)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when the first country is selected then the fields are kept`() {
        // GIVEN
        val state = createReducer().reduce(createState(address = null), AddressIntent.UpdateStreet("Main Street 1"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals("Main Street 1", result.street.text)
    }

    @Test
    fun `when the first country has states then it gets their layout`() {
        // GIVEN
        val params = createParams(supportedCountryCodes = setOf("US"))
        val state = createState(address = null, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertEquals(AddressSpec.US, result.spec)
        assertEquals(RequirementPolicy.Optional, result.houseNumberOrName.requirementPolicy)
    }

    @Test
    fun `when no loaded country is supported then no country is selected`() {
        // GIVEN
        val params = createParams(supportedCountryCodes = setOf("XX"))
        val state = createState(address = null, params)

        // WHEN
        val result = createReducer(params).reduce(state, AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)))

        // THEN
        assertNull(result.country.selected)
    }

    @Test
    fun `when loading the countries fails without a selected country then no country is selected`() {
        // GIVEN
        val state = createState(address = null)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.CountriesLoaded(Result.failure(IOException())))

        // THEN
        assertNull(result.country.selected)
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
    }

    @Test
    fun `when the countries fail to load after they loaded then the loaded countries are kept`() {
        // GIVEN
        val reducer = createReducer()
        val state = reducer.reduce(
            createState(US_ADDRESS),
            AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)),
        )

        // WHEN
        val result = reducer.reduce(state, AddressIntent.CountriesLoaded(Result.failure(IOException())))

        // THEN
        assertEquals(state, result)
    }

    @Test
    fun `when the states are loaded with the selected state then it stays selected`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals(US_STATES, result.states)
        assertEquals("CA", result.stateOrProvince.selected)
    }

    @Test
    fun `when the states are loaded without the selected state then the selection is cleared`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(stateOrProvince = "TX"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals(US_STATES, result.states)
        assertNull(result.stateOrProvince.selected)
    }

    /** As the country, a saved state still works offline. */
    @Test
    fun `when loading the states fails then the selected state is kept`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.failure(IOException())))

        // THEN
        assertEquals(emptyList<AddressRegion>(), result.states)
        assertEquals("CA", result.stateOrProvince.selected)
    }

    /** A late answer of an earlier request, e.g. after switching US → CA → US, must not replace the loaded states. */
    @Test
    fun `when loading the states fails once the states are loaded then the answer is ignored`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.failure(IOException())))

        // THEN
        assertSame(state, result)
    }

    @Test
    fun `when other states arrive once the states are loaded then they are ignored`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStates(US_STATES)
        val otherStates = listOf(AddressItem(id = "TX", name = "Texas"))

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.StatesLoaded("US", Result.success(otherStates)))

        // THEN
        assertSame(state, result)
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
    fun `when reset without an address before the countries load then no country is selected`() {
        // GIVEN
        val state = createState(US_ADDRESS)

        // WHEN
        val result = createReducer().reduce(state, AddressIntent.Reset(address = null))

        // THEN
        assertNull(result.country.selected)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when reset without an address after the countries loaded then the first country is selected`() {
        // GIVEN
        val reducer = createReducer()
        val state = reducer.reduce(
            createState(US_ADDRESS),
            AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)),
        )

        // WHEN
        val result = reducer.reduce(state, AddressIntent.Reset(address = null))

        // THEN
        assertEquals("DE", result.country.selected)
        assertNull(result.stateOrProvince.selected)
    }

    @Test
    fun `when reset with an unsupported country after the countries loaded then the first country is selected`() {
        // GIVEN
        val params = createParams(supportedCountryCodes = setOf("NL", "US"))
        val reducer = createReducer(params)
        val state = reducer.reduce(
            createState(US_ADDRESS, params),
            AddressIntent.CountriesLoaded(Result.success(COUNTRY_ITEMS)),
        )

        // WHEN
        val result = reducer.reduce(state, AddressIntent.Reset(US_ADDRESS.copy(country = "DE")))

        // THEN
        assertEquals("NL", result.country.selected)
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
        val expected = createState(US_ADDRESS).copy(countries = loadedCountries.countries)
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
        val result = createReducer().reduce(
            state,
            AddressIntent.Reset(US_ADDRESS.copy(country = "CA", stateOrProvince = "QC")),
        )

        // THEN
        assertEquals(emptyList<AddressRegion>(), result.states)
        assertEquals("QC", result.stateOrProvince.selected)
    }

    @Test
    fun `when reset after another country was picked then the saved state is selected once its states load`() {
        // GIVEN
        val reducer = createReducer()
        val savedState = reducer.reduce(
            createState(US_ADDRESS).withStates(US_STATES),
            AddressIntent.UpdateStateOrProvince("NY"),
        )
        val unsavedState = reducer.reduce(savedState, AddressIntent.UpdateCountry("CA"))

        // WHEN
        val resetState = reducer.reduce(unsavedState, AddressIntent.Reset(NEW_YORK_ADDRESS))
        val result = reducer.reduce(resetState, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals("NY", result.stateOrProvince.selected)
    }

    @Test
    fun `when reset while the states are loading then the saved state is selected once they load`() {
        // GIVEN
        val reducer = createReducer()
        val loadingState = createState(NEW_YORK_ADDRESS)

        // WHEN
        val resetState = reducer.reduce(loadingState, AddressIntent.Reset(NEW_YORK_ADDRESS))
        val result = reducer.reduce(resetState, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals("NY", result.stateOrProvince.selected)
    }

    @Test
    fun `when reset after loading the states failed then the saved state is selected once they load`() {
        // GIVEN
        val reducer = createReducer()
        val failedState = reducer.reduce(
            createState(NEW_YORK_ADDRESS),
            AddressIntent.StatesLoaded("US", Result.failure(IOException())),
        )

        // WHEN
        val resetState = reducer.reduce(failedState, AddressIntent.Reset(NEW_YORK_ADDRESS))
        val result = reducer.reduce(resetState, AddressIntent.StatesLoaded("US", Result.success(US_STATE_ITEMS)))

        // THEN
        assertEquals("NY", result.stateOrProvince.selected)
    }

    private fun createParams(
        supportedCountryCodes: Set<String> = emptySet(),
    ) = AddressComponentParams(supportedCountryCodes = supportedCountryCodes)

    private fun createState(
        address: AddressModel?,
        params: AddressComponentParams = createParams(),
    ) = AddressComponentStateFactory(params).createState(address)

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

        private val NEW_YORK_ADDRESS = US_ADDRESS.copy(stateOrProvince = "NY")

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
