package com.adyen.checkout.redirect.internal.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NativeRedirectRequestTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = NativeRedirectRequest(
            redirectData = "testData",
            returnQueryString = "testReturnString",
        )

        val actual = NativeRedirectRequest.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("redirectData", "testData")
            .put("returnQueryString", "testReturnString")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("redirectData", "testData")
            .put("returnQueryString", "testReturnString")

        val actual = NativeRedirectRequest.SERIALIZER.deserialize(response)

        val expected = NativeRedirectRequest(
            redirectData = "testData",
            returnQueryString = "testReturnString",
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing and a field is missing, then an error is thrown`() {
        val response = JSONObject()
            .put("redirectData", "testData")

        assertThrows<ModelSerializationException> {
            NativeRedirectRequest.SERIALIZER.deserialize(response)
        }
    }
}
