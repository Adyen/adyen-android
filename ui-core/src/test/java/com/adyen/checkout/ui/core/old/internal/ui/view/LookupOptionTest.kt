/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.view

import com.adyen.checkout.components.core.AddressData
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.ui.core.internal.ui.view.LookupOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LookupOptionTest {

    @Test
    fun `when creating LookupOption, then title and subtitle are formatted`() {
        val option = LookupOption(createLookupAddress())

        assertEquals("street houseNumberOrName apartmentSuite", option.title)
        assertEquals("postalCode, city, stateOrProvince, country", option.subtitle)
    }

    private fun createLookupAddress() = LookupAddress(
        id = "id",
        address = AddressData(
            postalCode = "postalCode",
            street = "street",
            stateOrProvince = "stateOrProvince",
            houseNumberOrName = "houseNumberOrName",
            apartmentSuite = "apartmentSuite",
            city = "city",
            country = "country",
        ),
    )
}
