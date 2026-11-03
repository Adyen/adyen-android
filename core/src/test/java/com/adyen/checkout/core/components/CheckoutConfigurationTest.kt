/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.core.components

import android.os.Parcel
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class CheckoutConfigurationTest {

    @Test
    fun `when parcelized with all values, then all values are restored`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.LIVE_UNITED_STATES,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.FRANCE,
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
            showSubmitButton = false,
        )

        val actual = configuration.parcelize()

        assertEquals(Environment.LIVE_UNITED_STATES, actual.environment)
        assertEquals(TEST_CLIENT_KEY, actual.clientKey)
        assertEquals(Locale.FRANCE, actual.shopperLocale)
        assertEquals(AnalyticsLevel.NONE, actual.analyticsConfiguration?.level)
        assertEquals(false, actual.showSubmitButton)
    }

    @Test
    fun `when parcelized with only required values, then optional values are not set`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )

        val actual = configuration.parcelize()

        assertEquals(Environment.TEST, actual.environment)
        assertEquals(TEST_CLIENT_KEY, actual.clientKey)
        assertNull(actual.shopperLocale)
        assertNull(actual.analyticsConfiguration)
        assertNull(actual.showSubmitButton)
    }

    @Test
    fun `when parcelized, then configurations added through the configuration block are restored`() {
        val firstConfiguration = FirstTestConfiguration("first")
        val secondConfiguration = SecondTestConfiguration(2)
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ) {
            addConfiguration(firstConfiguration)
            addConfiguration(secondConfiguration)
        }

        val actual = configuration.parcelize()

        assertEquals(
            mapOf(
                FirstTestConfiguration::class.java.name to firstConfiguration,
                SecondTestConfiguration::class.java.name to secondConfiguration,
            ),
            actual.getAvailableConfigurations(),
        )
    }

    // Goes through writeParcelable and readParcelable, like the system does when it restores state.
    @Suppress("DEPRECATION")
    private fun CheckoutConfiguration.parcelize(): CheckoutConfiguration {
        val parcel = Parcel.obtain()
        try {
            parcel.writeParcelable(this, 0)
            parcel.setDataPosition(0)
            return requireNotNull(parcel.readParcelable(CheckoutConfiguration::class.java.classLoader))
        } finally {
            parcel.recycle()
        }
    }

    @Parcelize
    private data class FirstTestConfiguration(val value: String) : Configuration

    @Parcelize
    private data class SecondTestConfiguration(val value: Int) : Configuration

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
