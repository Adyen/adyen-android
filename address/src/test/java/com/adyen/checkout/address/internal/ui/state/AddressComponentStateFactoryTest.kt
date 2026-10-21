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
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AddressComponentStateFactoryTest {

    @Test
    fun `when there is no address then no country is selected before the countries load`() {
        // GIVEN
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("nl-NL"))

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertNull(state.country.selected)
        assertEquals(AddressSpec.DEFAULT, state.spec)
    }

    @Test
    fun `when the countries contain the shopper locale country then it is selected`() {
        // GIVEN
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("nl-NL"))

        // WHEN
        val state = factory.createState(address = null, countries = COUNTRIES)

        // THEN
        assertEquals("NL", state.country.selected)
    }

    @Test
    fun `when the countries do not contain the shopper locale country then no country is selected`() {
        // GIVEN
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("de-DE"))

        // WHEN
        val state = factory.createState(address = null, countries = COUNTRIES)

        // THEN
        assertNull(state.country.selected)
    }

    @Test
    fun `when the countries do not contain the address country then no country is selected`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS, countries = listOf(NETHERLANDS))

        // THEN
        assertNull(state.country.selected)
        assertEquals(AddressSpec.DEFAULT, state.spec)
    }

    @Test
    fun `when an address is given then every field is prefilled`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS, COUNTRIES)

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

    /** A state is only selected from its loaded states, which a new form does not have yet. */
    @Test
    fun `when an address with a state is given then no state is selected before the states load`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS, COUNTRIES)

        // THEN
        assertNull(state.stateOrProvince.selected)
        assertTrue(state.states.isEmpty())
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

    private fun createFactory(shopperLocale: Locale = Locale.US) = AddressComponentStateFactory(
        AddressComponentParams(shopperLocale = shopperLocale, supportedCountryCodes = emptySet()),
    )

    companion object {
        private val NETHERLANDS = AddressRegion(code = "NL", name = "Netherlands")

        private val COUNTRIES = listOf(NETHERLANDS, AddressRegion(code = "US", name = "United States"))

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
