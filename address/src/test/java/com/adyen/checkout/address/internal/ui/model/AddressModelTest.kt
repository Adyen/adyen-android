/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/11/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import com.adyen.checkout.core.components.data.Address
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressModelTest {

    @Test
    fun `when the address is complete then every field is in the payment address`() {
        // GIVEN
        val address = AddressModel(
            country = "US",
            street = "Market Street 1",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            city = "San Francisco",
            stateOrProvince = "CA",
        )

        // WHEN
        val result = address.toPaymentAddress()

        // THEN
        val expected = Address(
            city = "San Francisco",
            country = "US",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            stateOrProvince = "CA",
            street = "Market Street 1",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when a field is missing then the placeholder is sent`() {
        // GIVEN
        val address = AddressModel(
            country = "NL",
            street = null,
            houseNumberOrName = null,
            postalCode = null,
            city = null,
            stateOrProvince = null,
        )

        // WHEN
        val result = address.toPaymentAddress()

        // THEN
        val expected = Address(
            city = Address.ADDRESS_NULL_PLACEHOLDER,
            country = "NL",
            houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER,
            postalCode = Address.ADDRESS_NULL_PLACEHOLDER,
            stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER,
            street = Address.ADDRESS_NULL_PLACEHOLDER,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when there is no country then the country placeholder is sent`() {
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
        val result = address.toPaymentAddress()

        // THEN
        assertEquals(Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER, result.country)
    }
}
