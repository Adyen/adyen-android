package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.exception.ModelSerializationException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TwintSdkDataTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = TwintSdkData(
            token = "testToken",
            isStored = true,
        )

        val actual = TwintSdkData.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("token", "testToken")
            .put("isStored", true)

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("token", "testToken")
            .put("isStored", true)

        val actual = TwintSdkData.SERIALIZER.deserialize(response)

        val expected = TwintSdkData(
            token = "testToken",
            isStored = true,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing and a field is missing, then an error is thrown`() {
        val response = JSONObject()

        assertThrows<ModelSerializationException> {
            TwintSdkData.SERIALIZER.deserialize(response)
        }
    }
}
