/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DisableTokenRequestTest {

    @Test
    fun `when serializing, then all fields are serialized`() {
        val request = DisableTokenRequest(
            sessionData = "session-data",
            storedPaymentMethodId = "stored-payment-method-id",
        )

        val actual = DisableTokenRequest.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("sessionData", "session-data")
            .put("storedPaymentMethodId", "stored-payment-method-id")
        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields are deserialized`() {
        val json = JSONObject()
            .put("sessionData", "session-data")
            .put("storedPaymentMethodId", "stored-payment-method-id")

        val actual = DisableTokenRequest.SERIALIZER.deserialize(json)

        val expected = DisableTokenRequest(
            sessionData = "session-data",
            storedPaymentMethodId = "stored-payment-method-id",
        )
        assertEquals(expected, actual)
    }
}
