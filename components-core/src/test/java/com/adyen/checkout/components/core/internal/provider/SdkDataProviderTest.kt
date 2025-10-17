/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.provider

import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.model.sdkData.SdkData
import com.adyen.checkout.core.internal.data.model.ModelUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SdkDataProviderTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var sdkDataProvider: SdkDataProvider

    @BeforeEach
    fun setup() {
        analyticsManager = TestAnalyticsManager()
        sdkDataProvider = SdkDataProvider(analyticsManager)
    }

    @Test
    fun `when createEncodedSdkData is called, then a non-null string is returned`() {
        analyticsManager.setCheckoutAttemptId("test_checkout_attempt_id")

        val encodedSdkData = sdkDataProvider.createEncodedSdkData()

        assertNotNull(encodedSdkData)
    }

    @Test
    fun `when createEncodedSdkData is called with 3DS2 SDK version, then a non-null string is returned`() {
        analyticsManager.setCheckoutAttemptId("test_checkout_attempt_id")

        val encodedSdkData = sdkDataProvider.createEncodedSdkData("test_3ds2_sdk_version")

        assertNotNull(encodedSdkData)
    }

    @Test
    fun `when createEncodedSdkData is called, then the checkoutAttemptId is correctly set`() {
        val checkoutAttemptId = "my_checkout_attempt_id"
        analyticsManager.setCheckoutAttemptId(checkoutAttemptId)

        val encodedSdkData = sdkDataProvider.createEncodedSdkData()

        val decodedSdkData = ModelUtils.deserializeAndDecodeOpt(encodedSdkData, SdkData.SERIALIZER)
        assertEquals(checkoutAttemptId, decodedSdkData?.analytics?.checkoutAttemptId)
    }

    @Test
    fun `when createEncodedSdkData is called with 3DS2 SDK version, then the version is correctly set`() {
        val threeDS2SdkVersion = "2.2.0"
        analyticsManager.setCheckoutAttemptId("test_checkout_attempt_id")

        val encodedSdkData = sdkDataProvider.createEncodedSdkData(threeDS2SdkVersion)

        val decodedSdkData = ModelUtils.deserializeAndDecodeOpt(encodedSdkData, SdkData.SERIALIZER)
        assertEquals(threeDS2SdkVersion, decodedSdkData?.authentication?.threeDS2SdkVersion)
    }
}
