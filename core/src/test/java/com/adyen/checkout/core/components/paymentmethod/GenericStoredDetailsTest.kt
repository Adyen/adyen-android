/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/8/2026.
 */

package com.adyen.checkout.core.components.paymentmethod

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class GenericStoredDetailsTest {

    @Test
    fun `when serializing then all fields are in the json`() {
        val details = GenericStoredDetails(
            type = "test_payment_method",
            sdkData = "test_sdk_data",
            storedPaymentMethodId = "stored_pm_id",
        )

        val json = GenericStoredDetails.SERIALIZER.serialize(details)

        assertEquals("test_payment_method", json.getString("type"))
        assertEquals("test_sdk_data", json.getString("sdkData"))
        assertEquals("stored_pm_id", json.getString("storedPaymentMethodId"))
    }

    @Test
    fun `when serializing with null fields then they are omitted from the json`() {
        val details = GenericStoredDetails(
            type = "test_payment_method",
            sdkData = null,
            storedPaymentMethodId = null,
        )

        val json = GenericStoredDetails.SERIALIZER.serialize(details)

        assertEquals("test_payment_method", json.getString("type"))
        assertFalse(json.has("sdkData"))
        assertFalse(json.has("storedPaymentMethodId"))
    }
}
