/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

internal class AddressSecondaryContentEntryTest {

    @ParameterizedTest
    @EnumSource(AddressSecondaryContentEntry::class)
    fun `when the key belongs to the address component then the entry is found`(entry: AddressSecondaryContentEntry) {
        // WHEN
        val result = AddressSecondaryContentEntry.fromKey(entry.key)

        // THEN
        assertEquals(entry, result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["INSTALLMENTS", "COUNTRY_CODE_PICKER", "FORM", "COUNTRY_PICKER", ""])
    fun `when the key belongs to the host then no entry is found`(key: String) {
        // WHEN
        val result = AddressSecondaryContentEntry.fromKey(key)

        // THEN
        assertNull(result)
    }
}
