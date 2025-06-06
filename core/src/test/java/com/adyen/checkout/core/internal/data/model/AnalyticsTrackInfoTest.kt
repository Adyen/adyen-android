package com.adyen.checkout.core.internal.data.model

import com.adyen.checkout.core.exception.ModelSerializationException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AnalyticsTrackInfoTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = AnalyticsTrackInfo(
            id = "id",
            timestamp = 12345L,
            component = "dropin",
            type = "test",
            target = "field",
            isStoredPaymentMethod = true,
            brand = "nike",
            issuer = "ing",
            validationErrorCode = "418",
            validationErrorMessage = "I'm a teapot",
            mapOf("test" to "something"),
        )

        val actual = AnalyticsTrackInfo.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("id", "id")
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("target", "field")
            .put("isStoredPaymentMethod", true)
            .put("brand", "nike")
            .put("issuer", "ing")
            .put("validationErrorCode", "418")
            .put("validationErrorMessage", "I'm a teapot")
            .put(
                "configData",
                JSONObject()
                    .put("test", "something"),
            )

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("id", "id")
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("target", "field")
            .put("isStoredPaymentMethod", true)
            .put("brand", "nike")
            .put("issuer", "ing")
            .put("validationErrorCode", "418")
            .put("validationErrorMessage", "I'm a teapot")
            .put(
                "configData",
                JSONObject()
                    .put("test", "something"),
            )

        val actual = AnalyticsTrackInfo.SERIALIZER.deserialize(response)

        val expected = AnalyticsTrackInfo(
            id = "id",
            timestamp = 12345L,
            component = "dropin",
            type = "test",
            target = "field",
            isStoredPaymentMethod = true,
            brand = "nike",
            issuer = "ing",
            validationErrorCode = "418",
            validationErrorMessage = "I'm a teapot",
            mapOf("test" to "something"),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing and a required field is missing, then an error is thrown`() {
        val response = JSONObject()
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("target", "field")
            .put("isStoredPaymentMethod", true)
            .put("brand", "nike")
            .put("issuer", "ing")
            .put("validationErrorCode", "418")
            .put("validationErrorMessage", "I'm a teapot")

        assertThrows<ModelSerializationException> {
            AnalyticsTrackInfo.SERIALIZER.deserialize(response)
        }
    }
}
