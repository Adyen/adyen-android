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
        val address = US_ADDRESS.copy(houseNumberOrName = null)

        // WHEN
        val result = address.format(Locale.US)

        // THEN
        assertEquals("Market Street 1, San Francisco, CA, 94107, United States", result)
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
        assertEquals("Main Street, 1, 1234AB, Amsterdam", result)
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

        @JvmStatic
        fun countryOrderSource() = listOf(
            arguments(US_ADDRESS, "Market Street 1, Apt 4, San Francisco, CA, 94107, United States"),
            arguments(
                AddressModel(
                    country = "GB",
                    street = "Downing Street",
                    houseNumberOrName = "10",
                    postalCode = "SW1A 2AA",
                    city = "London",
                    stateOrProvince = null,
                ),
                "10, Downing Street, London, SW1A 2AA, United Kingdom",
            ),
            arguments(
                AddressModel(
                    country = "JP",
                    street = "Marunouchi 1-1",
                    houseNumberOrName = "Tower 5F",
                    postalCode = "100-0005",
                    city = "Chiyoda",
                    stateOrProvince = "13",
                ),
                "100-0005, 13, Chiyoda, Marunouchi 1-1, Tower 5F, Japan",
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
                "Simon Carmiggeltstraat, 6, 1011 DJ, Amsterdam, Netherlands",
            ),
        )

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            arguments(Locale.US, "Market Street 1, Apt 4, San Francisco, CA, 94107, United States"),
            arguments(
                Locale.forLanguageTag("nl-NL"),
                "Market Street 1, Apt 4, San Francisco, CA, 94107, Verenigde Staten",
            ),
        )
    }
}
