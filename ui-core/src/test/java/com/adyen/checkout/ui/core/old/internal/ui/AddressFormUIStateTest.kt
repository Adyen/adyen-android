/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.old.internal.ui.model.Required
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressFormUIStateTest {

    @Test
    fun `when fromAddressParams is called with None AddressFormUIState should be NONE`() {
        assertEquals(
            AddressFormUIState.NONE,
            AddressFormUIState.fromAddressParams(AddressParams.None),
        )
    }

    @Test
    fun `when fromAddressParams is called with PostalCode AddressFormUIState should be POSTAL_CODE`() {
        assertEquals(
            AddressFormUIState.POSTAL_CODE,
            AddressFormUIState.fromAddressParams(AddressParams.PostalCode(addressFieldPolicy = Required())),
        )
    }

    @Test
    fun `when fromAddressParams is called with FullAddress AddressFormUIState should be FULL_ADDRESS`() {
        assertEquals(
            AddressFormUIState.FULL_ADDRESS,
            AddressFormUIState.fromAddressParams(AddressParams.FullAddress(addressFieldPolicy = Required())),
        )
    }

    @Test
    fun `when fromAddressParams is called with Lookup AddressFormUIState should be LOOKUP`() {
        assertEquals(
            AddressFormUIState.LOOKUP,
            AddressFormUIState.fromAddressParams(AddressParams.Lookup()),
        )
    }
}
