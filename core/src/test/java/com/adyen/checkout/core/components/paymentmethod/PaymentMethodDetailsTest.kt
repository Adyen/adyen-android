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

internal class PaymentMethodDetailsTest {

    @Test
    fun `when serializing stored details then the stored serializer is used instead of the one for the type`() {
        val details = GenericStoredDetails(
            type = BlikDetails.PAYMENT_METHOD_TYPE,
            sdkData = "test_sdk_data",
            storedPaymentMethodId = "stored_pm_id",
        )

        val json = PaymentMethodDetails.SERIALIZER.serialize(details)

        assertEquals(BlikDetails.PAYMENT_METHOD_TYPE, json.getString("type"))
        assertEquals("test_sdk_data", json.getString("sdkData"))
        assertEquals("stored_pm_id", json.getString("storedPaymentMethodId"))
        assertFalse(json.has("blikCode"))
    }

    @Test
    fun `when serializing regular details then the serializer for the type is used`() {
        val details = BlikDetails(
            type = BlikDetails.PAYMENT_METHOD_TYPE,
            sdkData = "test_sdk_data",
            blikCode = "123456",
            storedPaymentMethodId = null,
        )

        val json = PaymentMethodDetails.SERIALIZER.serialize(details)

        assertEquals(BlikDetails.PAYMENT_METHOD_TYPE, json.getString("type"))
        assertEquals("test_sdk_data", json.getString("sdkData"))
        assertEquals("123456", json.getString("blikCode"))
        assertFalse(json.has("storedPaymentMethodId"))
    }
}
