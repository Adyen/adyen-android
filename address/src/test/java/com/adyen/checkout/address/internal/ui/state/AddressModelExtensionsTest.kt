/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/11/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class AddressModelExtensionsTest {

    @ParameterizedTest
    @MethodSource("countryOrderSource")
    fun `when the address is formatted then the fields follow the country order`(
        address: AddressModel,
        expected: String,
    ) {
        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `when the address is formatted then the country name is in the shopper locale`(
        shopperLocale: Locale,
        expected: String,
    ) {
        // WHEN
        val result = US_ADDRESS.format(shopperLocale)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `when a field is missing then it is left out`() {
        // GIVEN
        val address = US_ADDRESS.copy(city = null)

        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals("Market Street 1 Apt 4, CA, 94107, United States", result)
    }

    @ParameterizedTest
    @MethodSource("partialStreetLineSource")
    fun `when only the street or only the house number is set then it is shown on its own`(
        address: AddressModel,
        expected: String,
    ) {
        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `when the street and the house number are missing then there is no street line`() {
        // GIVEN
        val address = US_ADDRESS.copy(street = null, houseNumberOrName = null)

        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals("San Francisco, CA, 94107, United States", result)
    }

    @Test
    fun `when there is no country then only the fields are formatted`() {
        // GIVEN
        val address = AddressModel(
            country = null,
            street = "Main Street",
            houseNumberOrName = "1",
            postalCode = "1234AB",
            city = "Amsterdam",
            stateOrProvince = null,
        )

        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals("Main Street 1, 1234AB, Amsterdam", result)
    }

    @Test
    fun `when only the country is set then only its name is shown`() {
        // GIVEN
        val address = EMPTY_ADDRESS.copy(country = "NL")

        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals("Netherlands", result)
    }

    @Test
    fun `when nothing is set then the summary is empty`() {
        // WHEN
        val result = EMPTY_ADDRESS.format(Locale.US)

        // THEN
        assertEquals("", result)
    }

    companion object {
        private val US_ADDRESS = AddressModel(
            country = "US",
            street = "Market Street 1",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            city = "San Francisco",
            stateOrProvince = "CA",
        )

        private val GB_ADDRESS = AddressModel(
            country = "GB",
            street = "Downing Street",
            houseNumberOrName = "10",
            postalCode = "SW1A 2AA",
            city = "London",
            stateOrProvince = null,
        )

        private val EMPTY_ADDRESS = AddressModel(
            country = null,
            street = null,
            houseNumberOrName = null,
            postalCode = null,
            city = null,
            stateOrProvince = null,
        )

        @JvmStatic
        fun countryOrderSource() = listOf(
            arguments(US_ADDRESS, "Market Street 1 Apt 4, San Francisco, CA, 94107, United States"),
            arguments(GB_ADDRESS, "10 Downing Street, London, SW1A 2AA, United Kingdom"),
            arguments(
                AddressModel(
                    country = "JP",
                    street = "Marunouchi 1-1",
                    houseNumberOrName = "Tower 5F",
                    postalCode = "100-0005",
                    city = "Chiyoda",
                    stateOrProvince = "13",
                ),
                "100-0005, 13, Chiyoda, Marunouchi 1-1 Tower 5F, Japan",
            ),
            arguments(
                AddressModel(
                    country = "AU",
                    street = "200 George Street",
                    houseNumberOrName = "Level 5",
                    postalCode = "2000",
                    city = "Sydney",
                    stateOrProvince = "NSW",
                ),
                "200 George Street Level 5, Sydney, NSW, 2000, Australia",
            ),
            arguments(
                AddressModel(
                    country = "BR",
                    street = "Avenida Paulista",
                    houseNumberOrName = "1000",
                    postalCode = "01310-100",
                    city = "São Paulo",
                    stateOrProvince = "SP",
                ),
                "Avenida Paulista 1000, 01310-100, São Paulo, SP, Brazil",
            ),
            arguments(
                AddressModel(
                    country = "CA",
                    street = "100 Queen Street W",
                    houseNumberOrName = "Suite 200",
                    postalCode = "M5H 2N2",
                    city = "Toronto",
                    stateOrProvince = "ON",
                ),
                "100 Queen Street W Suite 200, Toronto, M5H 2N2, ON, Canada",
            ),
            arguments(
                AddressModel(
                    country = "NL",
                    street = "Simon Carmiggeltstraat",
                    houseNumberOrName = "6",
                    postalCode = "1011 DJ",
                    city = "Amsterdam",
                    stateOrProvince = null,
                ),
                "Simon Carmiggeltstraat 6, 1011 DJ, Amsterdam, Netherlands",
            ),
            arguments(
                AddressModel(
                    country = "DE",
                    street = "Friedrichstraße",
                    houseNumberOrName = "43",
                    postalCode = "10117",
                    city = "Berlin",
                    stateOrProvince = null,
                ),
                "Friedrichstraße 43, 10117, Berlin, Germany",
            ),
        )

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            arguments(Locale.US, "Market Street 1 Apt 4, San Francisco, CA, 94107, United States"),
            arguments(
                Locale.forLanguageTag("nl-NL"),
                "Market Street 1 Apt 4, San Francisco, CA, 94107, Verenigde Staten",
            ),
        )

        @JvmStatic
        fun partialStreetLineSource() = listOf(
            arguments(
                US_ADDRESS.copy(houseNumberOrName = null),
                "Market Street 1, San Francisco, CA, 94107, United States",
            ),
            arguments(US_ADDRESS.copy(street = null), "Apt 4, San Francisco, CA, 94107, United States"),
            arguments(GB_ADDRESS.copy(houseNumberOrName = null), "Downing Street, London, SW1A 2AA, United Kingdom"),
            arguments(GB_ADDRESS.copy(street = null), "10, London, SW1A 2AA, United Kingdom"),
        )
    }
}
