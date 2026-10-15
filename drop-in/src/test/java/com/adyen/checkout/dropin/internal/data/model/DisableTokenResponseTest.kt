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

internal class DisableTokenResponseTest {

    @Test
    fun `when serializing, then all fields are serialized`() {
        val response = DisableTokenResponse(sessionData = "session-data")

        val actual = DisableTokenResponse.SERIALIZER.serialize(response)

        val expected = JSONObject().put("sessionData", "session-data")
        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields are deserialized`() {
        val json = JSONObject().put("sessionData", "session-data")

        val actual = DisableTokenResponse.SERIALIZER.deserialize(json)

        assertEquals(DisableTokenResponse(sessionData = "session-data"), actual)
    }

    @Test
    fun `when deserializing without session data, then session data is null`() {
        val actual = DisableTokenResponse.SERIALIZER.deserialize(JSONObject())

        assertEquals(DisableTokenResponse(sessionData = null), actual)
    }
}
