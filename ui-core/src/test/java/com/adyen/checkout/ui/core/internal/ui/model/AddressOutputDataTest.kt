/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/1/2024.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressOutputDataTest {
    @Test
    fun addressOutputDataToString() {
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
        )

        val expected = "street houseNumberOrName apartmentSuite postalCode city stateOrProvince country"
        assertEquals(expected, addressOutputData.toString())
    }
}
