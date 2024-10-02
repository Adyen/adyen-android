package com.adyen.checkout.components.core.paymentmethod

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TwintPaymentMethodTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = TwintPaymentMethod(
            type = "twint",
            checkoutAttemptId = "checkoutAttemptId",
            subtype = "sdk",
            storedPaymentMethodId = "storedPaymentMethodId",
        )

        val actual = TwintPaymentMethod.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("type", "twint")
            .put("checkoutAttemptId", "checkoutAttemptId")
            .put("subtype", "sdk")
            .put("storedPaymentMethodId", "storedPaymentMethodId")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("type", "twint")
            .put("checkoutAttemptId", "checkoutAttemptId")
            .put("subtype", "sdk")
            .put("storedPaymentMethodId", "storedPaymentMethodId")

        val actual = TwintPaymentMethod.SERIALIZER.deserialize(response)

        val expected = TwintPaymentMethod(
            type = "twint",
            checkoutAttemptId = "checkoutAttemptId",
            subtype = "sdk",
            storedPaymentMethodId = "storedPaymentMethodId",
        )

        assertEquals(expected, actual)
    }
}
