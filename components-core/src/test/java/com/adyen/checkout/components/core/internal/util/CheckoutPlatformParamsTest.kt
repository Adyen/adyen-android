package com.adyen.checkout.components.core.internal.util

import com.adyen.checkout.components.core.BuildConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CheckoutPlatformParamsTest {

    @BeforeEach
    fun setup() {
        CheckoutPlatformParams.resetDefaults()
    }

    @AfterEach
    fun cleanup() {
        CheckoutPlatformParams.resetDefaults()
    }

    @Test
    fun `when no overriding, then default are returned`() {
        assertEquals(CheckoutPlatform.ANDROID, CheckoutPlatformParams.platform)
        assertEquals(BuildConfig.CHECKOUT_VERSION, CheckoutPlatformParams.version)
    }

    @Test
    fun `when overriding, then set values are returned`() {
        CheckoutPlatformParams.overrideForCrossPlatform(
            CheckoutPlatform.FLUTTER,
            "test version",
        )

        assertEquals(CheckoutPlatform.FLUTTER, CheckoutPlatformParams.platform)
        assertEquals("test version", CheckoutPlatformParams.version)
    }
}
