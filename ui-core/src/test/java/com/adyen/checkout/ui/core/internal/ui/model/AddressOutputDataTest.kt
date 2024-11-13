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
import java.util.Locale

class AddressOutputDataTest {
    @Test
    fun addressOutputDataToString() {
        val addressOutputData = AddressOutputData(
            postalCode = FieldState("1234AB", Validation.Valid),
            houseNumberOrName = FieldState("1", Validation.Valid),
            apartmentSuite = FieldState("A", Validation.Valid),
            street = FieldState("Straat", Validation.Valid),
            city = FieldState("Amsterdam", Validation.Valid),
            stateOrProvince = FieldState("Noord-Holland", Validation.Valid),
            country = FieldState("NL", Validation.Valid),
            isOptional = false,
            countryOptions = emptyList(),
            stateOptions = emptyList(),
        )

        val expected = "Straat 1 A 1234AB Amsterdam Noord-Holland Netherlands"
        assertEquals(expected, addressOutputData.getDisplayAddress(Locale.ENGLISH))
    }
}
