/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AddressComponentStateFactoryTest {

    /** The first loaded country is only known once the countries load, so the reducer selects it. */
    @Test
    fun `when there is no address then no country is selected before the countries load`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertNull(state.country.selected)
        assertEquals(AddressSpec.DEFAULT, state.spec)
    }

    @Test
    fun `when the address country is not supported then no country is selected`() {
        // GIVEN
        val factory = createFactory(supportedCountryCodes = setOf("NL", "DE"))

        // WHEN
        val state = factory.createState(US_ADDRESS)

        // THEN
        assertNull(state.country.selected)
        assertEquals(AddressSpec.DEFAULT, state.spec)
        assertNull(state.stateOrProvince.selected)
    }

    @Test
    fun `when an address is given then every field is prefilled`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS)

        // THEN
        assertEquals("US", state.country.selected)
        assertEquals(AddressSpec.US, state.spec)
        assertEquals("Main Street 1", state.street.text)
        assertEquals("Apt 4", state.houseNumberOrName.text)
        assertEquals("94107", state.postalCode.text)
        assertEquals("San Francisco", state.city.text)
        assertEquals(RequirementPolicy.Required, state.street.requirementPolicy)
        assertEquals(RequirementPolicy.Optional, state.houseNumberOrName.requirementPolicy)
    }

    /** As the country, the state is kept as a code until its states load. */
    @Test
    fun `when an address with a state is given then the state is selected before the states load`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS)

        // THEN
        assertEquals("CA", state.stateOrProvince.selected)
        assertTrue(state.states.isEmpty())
    }

    @Test
    fun `when an address of a country without states is given then no state is selected`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS.copy(country = "NL"))

        // THEN
        assertNull(state.stateOrProvince.selected)
    }

    @Test
    fun `when the countries are not loaded yet then the country list is empty`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertTrue(state.countries.isEmpty())
    }

    private fun createFactory(
        supportedCountryCodes: Set<String> = emptySet(),
    ) = AddressComponentStateFactory(AddressComponentParams(supportedCountryCodes = supportedCountryCodes))

    companion object {
        private val US_ADDRESS = AddressModel(
            country = "US",
            street = "Main Street 1",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            city = "San Francisco",
            stateOrProvince = "CA",
        )
    }
}
