/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.mbway.internal.model

import com.adyen.checkout.mbway.internal.ui.model.MBWayTransformerRegistry
import com.adyen.checkout.mbway.internal.ui.state.MBWayFieldId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayTransformerRegistryTest {
    private lateinit var transformerRegistry: MBWayTransformerRegistry

    @BeforeEach
    fun setup() {
        transformerRegistry = MBWayTransformerRegistry()
    }

    @Test
    fun `when transform is called for phone number, then transformer should remove leading zeros`() {
        val phoneNumber = "0123456789"
        val transformedPhoneNumber = transformerRegistry.transform(MBWayFieldId.PHONE_NUMBER, phoneNumber)

        assertEquals("123456789", transformedPhoneNumber)
    }

    @Test
    fun `when transform is called for country code, then transformer should return the value as is`() {
        val countryCode = "US"
        val transformedCountryCode = transformerRegistry.transform(MBWayFieldId.COUNTRY_CODE, countryCode)

        assertEquals("US", transformedCountryCode)
    }

    @Test
    fun `when transform is called for phone number with an empty value, then transformer should return empty string`() {
        val phoneNumber = "0"
        val transformedPhoneNumber = transformerRegistry.transform(MBWayFieldId.PHONE_NUMBER, phoneNumber)

        assertEquals("", transformedPhoneNumber)
    }

    @Test
    fun `when transform is called for phone number with no leading zeros, then transformer should return the value`() {
        val phoneNumber = "123456789"
        val transformedPhoneNumber = transformerRegistry.transform(MBWayFieldId.PHONE_NUMBER, phoneNumber)

        assertEquals("123456789", transformedPhoneNumber)
    }
}
