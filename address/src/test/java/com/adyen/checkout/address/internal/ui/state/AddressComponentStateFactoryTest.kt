/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AddressComponentStateFactoryTest {

    @Test
    fun `when no supported countries are configured, then every country can be picked`() {
        val factory = createFactory(supportedCountryCodes = emptySet())

        val actual = factory.createInitialState().countries

        assertEquals(CountryUtils.getCountries().size, actual.size)
    }

    @Test
    fun `when supported countries are configured, then only those can be picked`() {
        val factory = createFactory(supportedCountryCodes = setOf("NL", "US"))

        val actual = factory.createInitialState().countries.map { it.isoCode }.toSet()

        assertEquals(setOf("NL", "US"), actual)
    }

    @Test
    fun `when the countries are listed, then their names follow the shopper locale`() {
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("nl-NL"), supportedCountryCodes = setOf("DE"))

        val actual = factory.createInitialState().countries.single().countryName

        assertEquals("Duitsland", actual)
    }

    @Test
    fun `when the country of the shopper locale is supported, then it is selected initially`() {
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("en-US"), supportedCountryCodes = emptySet())

        val actual = factory.createInitialState().country.selectedCountry?.isoCode

        assertEquals("US", actual)
    }

    @Test
    fun `when the country of the shopper locale is not supported, then no country is selected initially`() {
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("en-US"), supportedCountryCodes = setOf("NL"))

        val actual = factory.createInitialState().country.selectedCountry

        assertNull(actual)
    }

    @Test
    fun `when the initial state is created, then the postal code is empty`() {
        val factory = createFactory()

        val actual = factory.createInitialState().postalCode

        assertEquals(TextInputComponentState(), actual)
    }

    @Test
    fun `when a state is created from an address, then the address is prefilled`() {
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("en-US"), supportedCountryCodes = emptySet())

        val actual = factory.createState(AddressModel(country = "NL", postalCode = "1234 AB"))

        assertEquals("NL", actual.country.selectedCountry?.isoCode)
        assertEquals("1234 AB", actual.postalCode.text)
    }

    @Test
    fun `when a state is created from an address with an unsupported country, then no country is selected`() {
        val factory = createFactory(supportedCountryCodes = setOf("US"))

        val actual = factory.createState(AddressModel(country = "NL", postalCode = "1234 AB"))

        assertNull(actual.country.selectedCountry)
    }

    @Test
    fun `when a state is created from an address without a country, then the shopper locale country is selected`() {
        val factory = createFactory(shopperLocale = Locale.forLanguageTag("en-US"), supportedCountryCodes = emptySet())

        val actual = factory.createState(AddressModel(country = null, postalCode = "12345"))

        assertEquals("US", actual.country.selectedCountry?.isoCode)
    }

    @Test
    fun `when a state is created without an address, then it matches the initial state`() {
        val factory = createFactory()

        val actual = factory.createState(address = null)

        assertEquals(factory.createInitialState(), actual)
    }

    private fun createFactory(
        shopperLocale: Locale = Locale.forLanguageTag("en-US"),
        supportedCountryCodes: Set<String> = emptySet(),
    ) = AddressComponentStateFactory(
        componentParams = AddressComponentParams(
            shopperLocale = shopperLocale,
            supportedCountryCodes = supportedCountryCodes,
        ),
    )
}
