package com.adyen.checkout.components.core.action

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RedirectActionTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = RedirectAction(
            type = "type",
            paymentData = "paymentData",
            paymentMethodType = "paymentMethodType",
            method = "method",
            url = "url",
            nativeRedirectData = "nativeRedirectData",
        )

        val actual = RedirectAction.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("type", "type")
            .put("paymentData", "paymentData")
            .put("paymentMethodType", "paymentMethodType")
            .put("method", "method")
            .put("url", "url")
            .put("nativeRedirectData", "nativeRedirectData")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("type", "type")
            .put("paymentData", "paymentData")
            .put("paymentMethodType", "paymentMethodType")
            .put("method", "method")
            .put("url", "url")
            .put("nativeRedirectData", "nativeRedirectData")

        val actual = RedirectAction.SERIALIZER.deserialize(response)

        val expected = RedirectAction(
            type = "type",
            paymentData = "paymentData",
            paymentMethodType = "paymentMethodType",
            method = "method",
            url = "url",
            nativeRedirectData = "nativeRedirectData",
        )

        assertEquals(expected, actual)
    }
}
