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
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class CheckoutConfigurationTest {

    @Test
    fun `when configurations are added in the configuration block, then they are available`() {
        val firstConfiguration = FirstTestConfiguration("first")
        val secondConfiguration = SecondTestConfiguration(2)

        val actual = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ) {
            addConfiguration(firstConfiguration)
            addConfiguration(secondConfiguration)
        }

        assertEquals(
            mapOf(
                FirstTestConfiguration::class.java.name to firstConfiguration,
                SecondTestConfiguration::class.java.name to secondConfiguration,
            ),
            actual.getAvailableConfigurations(),
        )
    }

    @Test
    fun `when a configuration of the same type is added twice, then the last one is kept`() {
        val actual = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ) {
            addConfiguration(FirstTestConfiguration("first"))
            addConfiguration(FirstTestConfiguration("second"))
        }

        assertEquals(
            mapOf(FirstTestConfiguration::class.java.name to FirstTestConfiguration("second")),
            actual.getAvailableConfigurations(),
        )
    }

    @Test
    fun `when two configurations are created with the same values, then they are equal`() {
        val first = createConfiguration()
        val second = createConfiguration()

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `when two configurations have different configurations added in the block, then they are not equal`() {
        val first = createConfiguration()
        val second = CheckoutConfiguration(
            environment = Environment.LIVE_UNITED_STATES,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.FRANCE,
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
            showSubmitButton = false,
        ) {
            addConfiguration(FirstTestConfiguration("other"))
        }

        assertNotEquals(first, second)
    }

    @Test
    fun `when parcelized with all values, then an equal configuration is restored`() {
        val configuration = createConfiguration()

        val actual = configuration.parcelize()

        assertEquals(configuration, actual)
    }

    @Test
    fun `when parcelized with only required values, then an equal configuration is restored`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )

        val actual = configuration.parcelize()

        assertEquals(configuration, actual)
    }

    // Creates every value again on each call, so equality can't rely on shared instances.
    private fun createConfiguration() = CheckoutConfiguration(
        environment = Environment.LIVE_UNITED_STATES,
        clientKey = TEST_CLIENT_KEY,
        shopperLocale = Locale.FRANCE,
        analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        showSubmitButton = false,
    ) {
        addConfiguration(FirstTestConfiguration("first"))
        addConfiguration(SecondTestConfiguration(2))
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
