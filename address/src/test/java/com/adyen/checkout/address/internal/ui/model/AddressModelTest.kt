/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import com.adyen.checkout.core.components.data.Address
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressModelTest {

    @Test
    fun `when an address is mapped to the payment request, then fields the form does not collect get placeholders`() {
        val address = AddressModel(country = "NL", postalCode = "1234 AB")

        val actual = address.toPaymentAddress()

        val expected = Address(
            city = Address.ADDRESS_NULL_PLACEHOLDER,
            country = "NL",
            houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER,
            postalCode = "1234 AB",
            stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER,
            street = Address.ADDRESS_NULL_PLACEHOLDER,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when an address without values is mapped to the payment request, then every field gets a placeholder`() {
        val address = AddressModel()

        val actual = address.toPaymentAddress()

        val expected = Address(
            city = Address.ADDRESS_NULL_PLACEHOLDER,
            country = Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER,
            houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER,
            postalCode = Address.ADDRESS_NULL_PLACEHOLDER,
            stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER,
            street = Address.ADDRESS_NULL_PLACEHOLDER,
        )
        assertEquals(expected, actual)
    }
}
