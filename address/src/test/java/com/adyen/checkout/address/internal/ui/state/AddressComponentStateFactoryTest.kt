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
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AddressComponentStateFactoryTest {

    @Test
    fun `when there is no address then the shopper locale country is selected before the countries load`() {
        // GIVEN
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("nl-NL"))

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertEquals("NL", state.country.selected)
        assertEquals(AddressSpec.DEFAULT, state.spec)
        assertEquals(StateOrProvinceState.Loading(pendingCode = null), state.stateOrProvince)
    }

    @Test
    fun `when the shopper locale country is not supported then no country is selected`() {
        // GIVEN
        val factory = createFactory(
            shopperLocale = Locale.forLanguageTag("nl-NL"),
            supportedCountryCodes = setOf("US", "CA"),
        )

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertNull(state.country.selected)
        assertEquals(StateOrProvinceState.Unavailable, state.stateOrProvince)
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

    @Test
    fun `when an address with a state is given then the state is pending until the states load`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createState(US_ADDRESS)

        // THEN
        assertEquals(StateOrProvinceState.Loading(pendingCode = "CA"), state.stateOrProvince)
    }

    @Test
    fun `when the countries are not loaded yet then the country list is empty`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createInitialState()

        // THEN
        assertTrue(state.countries.isEmpty())
        assertTrue(state.isLoadingCountries)
    }

    private fun createFactory(
        shopperLocale: Locale = Locale.US,
        supportedCountryCodes: Set<String> = emptySet(),
    ) = AddressComponentStateFactory(
        AddressComponentParams(
            shopperLocale = shopperLocale,
            supportedCountryCodes = supportedCountryCodes,
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
    }
}
