package com.adyen.checkout.components.core.internal.data.model

import com.adyen.checkout.core.old.internal.data.model.ModelUtils
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
                configData = null,
            ),
        )
        val logs = listOf(
            AnalyticsTrackLog(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                subType = null,
                result = null,
                target = null,
                message = null,
            ),
        )
        val errors = listOf(
            AnalyticsTrackError(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                errorType = null,
                code = null,
                target = null,
                message = null,
            ),
        )
        val request = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = info,
            logs = logs,
            errors = errors,
        )

        val actual = AnalyticsTrackRequest.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("channel", "android")
            .put("platform", "android")
            .put("info", ModelUtils.serializeOptList(info, AnalyticsTrackInfo.SERIALIZER))
            .put("logs", ModelUtils.serializeOptList(logs, AnalyticsTrackLog.SERIALIZER))
            .put("errors", ModelUtils.serializeOptList(errors, AnalyticsTrackError.SERIALIZER))

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
                configData = null,
            ),
        )
        val logs = listOf(
            AnalyticsTrackLog(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                subType = null,
                result = null,
                target = null,
                message = null,
            ),
        )
        val errors = listOf(
            AnalyticsTrackError(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                errorType = null,
                code = null,
                target = null,
                message = null,
            ),
        )
        val response = JSONObject()
            .put("channel", "android")
            .put("platform", "android")
            .put("info", ModelUtils.serializeOptList(info, AnalyticsTrackInfo.SERIALIZER))
            .put("logs", ModelUtils.serializeOptList(logs, AnalyticsTrackLog.SERIALIZER))
            .put("errors", ModelUtils.serializeOptList(errors, AnalyticsTrackError.SERIALIZER))

        val actual = AnalyticsTrackRequest.SERIALIZER.deserialize(response)

        val expected = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = info,
            logs = logs,
            errors = errors,
        )

        assertEquals(expected, actual)
    }
}
