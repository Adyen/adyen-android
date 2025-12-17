/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/10/2025.
 */

package com.adyen.checkout.components.core.internal.provider

import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.core.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.internal.data.model.getLongOrNull
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal class SdkDataProviderTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var sdkDataProvider: DefaultSdkDataProvider

    @BeforeEach
    fun setup() {
        analyticsManager = TestAnalyticsManager()
        sdkDataProvider = DefaultSdkDataProvider(analyticsManager)
    }

    @Test
    fun `when createEncodedSdkData is called, then data is correctly set`() {
        val checkoutAttemptId = "test_checkout_attempt_id"
        val threeDS2SdkVersion = "2.2.0"
        analyticsManager.setCheckoutAttemptId(checkoutAttemptId)

        val encodedSdkData = sdkDataProvider.createEncodedSdkData(threeDS2SdkVersion)

        assertNotNull(encodedSdkData)
        val jsonObject = decodeJsonObject(encodedSdkData!!)
        assertEquals(
            checkoutAttemptId,
            jsonObject.optJSONObject("analytics")?.getStringOrNull("checkoutAttemptId"),
        )
        assertEquals(
            threeDS2SdkVersion,
            jsonObject.optJSONObject("authentication")?.getStringOrNull("threeDS2SdkVersion"),
        )
        assertNotNull(jsonObject.getLongOrNull("createdAt"))
        assertEquals(true, jsonObject.getBooleanOrNull("supportNativeRedirect"))
    }

    @Test
    fun `when createEncodedSdkData is called without 3DS2 SDK version, then the version is null`() {
        analyticsManager.setCheckoutAttemptId("test_checkout_attempt_id")

        val encodedSdkData = sdkDataProvider.createEncodedSdkData(null)

        assertNotNull(encodedSdkData)
        val jsonObject = decodeJsonObject(encodedSdkData!!)
        assertNull(jsonObject.optJSONObject("authentication")?.getStringOrNull("threeDS2SdkVersion"))
    }

    private fun decodeJsonObject(encodedString: String): JSONObject {
        val decodedString = Base64.decode(encodedString).toString(Charsets.UTF_8)
        return JSONObject(decodedString)
    }
}
