package com.adyen.checkout.core.internal.data.model

import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsTrackInfo
import com.adyen.checkout.core.analytics.internal.data.remote.model.AnalyticsTrackLog
import com.adyen.checkout.core.exception.ModelSerializationException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AnalyticsTrackLogTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = AnalyticsTrackLog(
            id = "id",
            timestamp = 12345L,
            component = "dropin",
            type = "test",
            subType = "subtest",
            result = "positive",
            target = "field",
            message = "Hello",
        )

        val actual = AnalyticsTrackLog.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("id", "id")
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("subType", "subtest")
            .put("result", "positive")
            .put("target", "field")
            .put("message", "Hello")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("id", "id")
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("subType", "subtest")
            .put("result", "positive")
            .put("target", "field")
            .put("message", "Hello")

        val actual = AnalyticsTrackLog.SERIALIZER.deserialize(response)

        val expected = AnalyticsTrackLog(
            id = "id",
            timestamp = 12345L,
            component = "dropin",
            type = "test",
            subType = "subtest",
            result = "positive",
            target = "field",
            message = "Hello",
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing and a required field is missing, then an error is thrown`() {
        val response = JSONObject()
            .put("timestamp", 12345L)
            .put("component", "dropin")
            .put("type", "test")
            .put("subType", "subtest")
            .put("target", "field")
            .put("message", "Hello")

        assertThrows<ModelSerializationException> {
            AnalyticsTrackInfo.SERIALIZER.deserialize(response)
        }
    }
}
