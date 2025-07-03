/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AnalyticsSetupResponseTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val request = AnalyticsSetupResponse(
            checkoutAttemptId = "random-id",
        )

        val actual = AnalyticsSetupResponse.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("checkoutAttemptId", "random-id")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val response = JSONObject()
            .put("checkoutAttemptId", "random-id")

        val actual = AnalyticsSetupResponse.SERIALIZER.deserialize(response)

        val expected = AnalyticsSetupResponse(
            checkoutAttemptId = "random-id",
        )

        assertEquals(expected, actual)
    }
}
