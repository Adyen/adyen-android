package com.adyen.checkout.redirect.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.redirect.old.internal.data.model.NativeRedirectResponse
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NativeRedirectResponseTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = NativeRedirectResponse(
            redirectResult = "testRedirectResult",
        )

        val actual = NativeRedirectResponse.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("redirectResult", "testRedirectResult")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("redirectResult", "testRedirectResult")

        val actual = NativeRedirectResponse.SERIALIZER.deserialize(response)

        val expected = NativeRedirectResponse(
            redirectResult = "testRedirectResult",
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing and a field is missing, then an error is thrown`() {
        val response = JSONObject()

        assertThrows<ModelSerializationException> {
            NativeRedirectResponse.SERIALIZER.deserialize(response)
        }
    }
}
