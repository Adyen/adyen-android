/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */

package com.adyen.checkout.core.internal.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AnalyticsTrackRequestTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val info = listOf(
            AnalyticsTrackInfo(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                target = null,
                isStoredPaymentMethod = null,
                brand = null,
                issuer = null,
                validationErrorCode = null,
                validationErrorMessage = null,
            ),
        )
        val logs = listOf(
            AnalyticsTrackLog(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                subType = null,
                target = null,
                message = null,
            ),
        )
        val request = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = info,
            logs = logs,
        )

        val actual = AnalyticsTrackRequest.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("channel", "android")
            .put("platform", "android")
            .put("info", ModelUtils.serializeOptList(info, AnalyticsTrackInfo.SERIALIZER))
            .put("logs", ModelUtils.serializeOptList(logs, AnalyticsTrackLog.SERIALIZER))

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val info = listOf(
            AnalyticsTrackInfo(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                target = null,
                isStoredPaymentMethod = null,
                brand = null,
                issuer = null,
                validationErrorCode = null,
                validationErrorMessage = null,
            ),
        )
        val logs = listOf(
            AnalyticsTrackLog(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                subType = null,
                target = null,
                message = null,
            ),
        )
        val response = JSONObject()
            .put("channel", "android")
            .put("platform", "android")
            .put("info", ModelUtils.serializeOptList(info, AnalyticsTrackInfo.SERIALIZER))
            .put("logs", ModelUtils.serializeOptList(logs, AnalyticsTrackLog.SERIALIZER))

        val actual = AnalyticsTrackRequest.SERIALIZER.deserialize(response)

        val expected = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = info,
            logs = logs,
        )

        assertEquals(expected, actual)
    }
}
