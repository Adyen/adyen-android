package com.adyen.checkout.components.core

import android.os.Parcel
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.old.Environment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
internal class CheckoutConfigurationTest {

    @Test
    fun `when parcelized, then it must be correctly deparcelized`() {
        val testConfiguration = TestConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build()
        val testActionConfiguration = TestConfiguration.Builder(Locale.CHINA, Environment.APSE, LIVE_CLIENT_KEY).build()
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
            addActionConfiguration(testActionConfiguration)
        }

        assertCorrectParcelization(checkoutConfiguration, testConfiguration, testActionConfiguration)
    }

    @Test
    fun `when parcelized with required fields only, then it must be correctly deparcelized`() {
        val testConfiguration = TestConfiguration.Builder(null, Environment.TEST, TEST_CLIENT_KEY).build()
        val testActionConfiguration = TestConfiguration.Builder(null, Environment.APSE, LIVE_CLIENT_KEY).build()
        val checkoutConfiguration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        ) {
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
            addActionConfiguration(testActionConfiguration)
        }

        assertCorrectParcelization(checkoutConfiguration, testConfiguration, testActionConfiguration)
    }

    private fun assertCorrectParcelization(
        checkoutConfiguration: CheckoutConfiguration,
        paymentMethodConfiguration: Configuration,
        actionConfiguration: Configuration
    ) {
        val parcel = Parcel.obtain()
        checkoutConfiguration.writeToParcel(parcel, 0)
        // Reset the parcel for reading
        parcel.setDataPosition(0)

        val deparcelized = CheckoutConfiguration.createFromParcel(parcel)
        assertConfigurationEquals(checkoutConfiguration, deparcelized)

        val deparcelizedPmConfig = deparcelized.getConfiguration<TestConfiguration>(TEST_CONFIGURATION_KEY)
        assertConfigurationEquals(paymentMethodConfiguration, requireNotNull(deparcelizedPmConfig))

        val deparcelizedActionConfig = deparcelized.getActionConfiguration(TestConfiguration::class.java)
        assertConfigurationEquals(actionConfiguration, requireNotNull(deparcelizedActionConfig))
    }

    private fun assertConfigurationEquals(expected: Configuration, actual: Configuration) {
        assertEquals(expected.shopperLocale, actual.shopperLocale)
        assertEquals(expected.environment, actual.environment)
        assertEquals(expected.clientKey, actual.clientKey)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.analyticsConfiguration, actual.analyticsConfiguration)
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val LIVE_CLIENT_KEY = "live_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
