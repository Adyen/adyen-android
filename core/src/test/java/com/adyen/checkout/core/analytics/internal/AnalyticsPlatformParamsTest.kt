/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import com.adyen.checkout.core.BuildConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AnalyticsPlatformParamsTest {

    @BeforeEach
    fun setup() {
        AnalyticsPlatformParams.resetToDefaults()
    }

    @AfterEach
    fun cleanup() {
        AnalyticsPlatformParams.resetToDefaults()
    }

    @Test
    fun `when no overriding, then default are returned`() {
        assertEquals(AnalyticsPlatform.ANDROID.value, AnalyticsPlatformParams.platform)
        assertEquals(BuildConfig.CHECKOUT_VERSION, AnalyticsPlatformParams.version)
    }

    @Test
    fun `when overriding, then set values are returned`() {
        AnalyticsPlatformParams.overrideForCrossPlatform(
            AnalyticsPlatform.FLUTTER,
            "test version",
        )

        assertEquals(AnalyticsPlatform.FLUTTER.value, AnalyticsPlatformParams.platform)
        assertEquals("test version", AnalyticsPlatformParams.version)
    }
}
