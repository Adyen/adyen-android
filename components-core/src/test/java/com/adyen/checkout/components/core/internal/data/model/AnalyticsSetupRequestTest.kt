package com.adyen.checkout.components.core.internal.data.model

import com.adyen.checkout.components.core.Amount
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AnalyticsSetupRequestTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = AnalyticsSetupRequest(
            version = "1.0.0",
            channel = "android",
            platform = "android",
            locale = "en-US",
            component = "dropin",
            flavor = "dropin",
            deviceBrand = "Google",
            deviceModel = "Pixel",
            referrer = "unknown",
            systemVersion = "15",
            containerWidth = 50,
            screenWidth = 100,
            paymentMethods = listOf("ideal", "scheme"),
            amount = Amount("EUR", 1337),
            sessionId = "session",
        )

        val actual = AnalyticsSetupRequest.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("version", "1.0.0")
            .put("channel", "android")
            .put("platform", "android")
            .put("locale", "en-US")
            .put("component", "dropin")
            .put("flavor", "dropin")
            .put("deviceBrand", "Google")
            .put("deviceModel", "Pixel")
            .put("referrer", "unknown")
            .put("systemVersion", "15")
            .put("containerWidth", 50)
            .put("screenWidth", 100)
            .put("paymentMethods", JSONArray(listOf("ideal", "scheme")))
            .put("amount", JSONObject().put("currency", "EUR").put("value", 1337))
            .put("sessionId", "session")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("version", "1.0.0")
            .put("channel", "android")
            .put("platform", "android")
            .put("locale", "en-US")
            .put("component", "dropin")
            .put("flavor", "dropin")
            .put("deviceBrand", "Google")
            .put("deviceModel", "Pixel")
            .put("referrer", "unknown")
            .put("systemVersion", "15")
            .put("containerWidth", 50)
            .put("screenWidth", 100)
            .put("paymentMethods", JSONArray(listOf("ideal", "scheme")))
            .put("amount", JSONObject().put("currency", "EUR").put("value", 1337))
            .put("sessionId", "session")

        val actual = AnalyticsSetupRequest.SERIALIZER.deserialize(response)

        val expected = AnalyticsSetupRequest(
            version = "1.0.0",
            channel = "android",
            platform = "android",
            locale = "en-US",
            component = "dropin",
            flavor = "dropin",
            deviceBrand = "Google",
            deviceModel = "Pixel",
            referrer = "unknown",
            systemVersion = "15",
            containerWidth = 50,
            screenWidth = 100,
            paymentMethods = listOf("ideal", "scheme"),
            amount = Amount("EUR", 1337),
            sessionId = "session",
        )

        assertEquals(expected, actual)
    }
}
