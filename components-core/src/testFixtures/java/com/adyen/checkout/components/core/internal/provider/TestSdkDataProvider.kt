/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/12/2025.
 */

package com.adyen.checkout.components.core.internal.provider

import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Test implementation of [SdkDataProvider] that allows controlling the returned SDK data
 * and verifying the parameters passed to [createEncodedSdkData].
 */
class TestSdkDataProvider : SdkDataProvider {

    private var sdkData: String? = TEST_SDK_DATA
    private var lastThreeDS2SdkVersion: String? = null

    override fun createEncodedSdkData(threeDS2SdkVersion: String?): String? {
        lastThreeDS2SdkVersion = threeDS2SdkVersion
        return sdkData
    }

    /**
     * Asserts that the last call to [createEncodedSdkData] was made with the expected threeDS2SdkVersion.
     */
    fun assertThreeDS2SdkVersionEquals(expected: String?) {
        assertEquals(expected, lastThreeDS2SdkVersion)
    }

    companion object {
        const val TEST_SDK_DATA = "test_sdk_data"
    }
}
