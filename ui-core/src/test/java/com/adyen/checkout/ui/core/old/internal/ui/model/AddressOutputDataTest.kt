/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressOutputDataTest {

    @Test
    fun `AddressOutputData is formatted, then a certain template is used`() {
        val addressOutputData = AddressOutputData(
            postalCode = FieldState("postalCode", Validation.Valid),
            houseNumberOrName = FieldState("houseNumberOrName", Validation.Valid),
            apartmentSuite = FieldState("apartmentSuite", Validation.Valid),
            street = FieldState("street", Validation.Valid),
            city = FieldState("city", Validation.Valid),
            stateOrProvince = FieldState("stateOrProvince", Validation.Valid),
            country = FieldState("country", Validation.Valid),
            isOptional = false,
            countryOptions = emptyList(),
            stateOptions = emptyList(),
            countryDisplayName = "countryDisplayName",
        )

        val expected = "street houseNumberOrName apartmentSuite\npostalCode, city, stateOrProvince, countryDisplayName"
        assertEquals(expected, addressOutputData.formatted())
    }
}
