package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.BuildConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AnalyticsPlatformParamsTest {

    @BeforeEach
    fun setup() {
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
