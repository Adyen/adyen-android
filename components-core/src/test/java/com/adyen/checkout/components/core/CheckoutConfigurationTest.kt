package com.adyen.checkout.components.core

import android.os.Parcel
import com.adyen.checkout.core.Environment
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
        val original = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
            addActionConfiguration(testActionConfiguration)
        }

        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        // Reset the parcel for reading
        parcel.setDataPosition(0)

        val deparcelized = CheckoutConfiguration.createFromParcel(parcel)

        assertEquals(original.shopperLocale, deparcelized.shopperLocale)
        assertEquals(original.environment, deparcelized.environment)
        assertEquals(original.clientKey, deparcelized.clientKey)
        assertEquals(original.amount, deparcelized.amount)
        assertEquals(original.analyticsConfiguration, deparcelized.analyticsConfiguration)

        val deparcelizedPmConfig = deparcelized.getConfiguration<TestConfiguration>(TEST_CONFIGURATION_KEY)
        assertEquals(testConfiguration.shopperLocale, deparcelizedPmConfig?.shopperLocale)
        assertEquals(testConfiguration.environment, deparcelizedPmConfig?.environment)
        assertEquals(testConfiguration.clientKey, deparcelizedPmConfig?.clientKey)
        assertEquals(testConfiguration.amount, deparcelizedPmConfig?.amount)
        assertEquals(testConfiguration.analyticsConfiguration, deparcelizedPmConfig?.analyticsConfiguration)

        val deparcelizedActionConfig = deparcelized.getActionConfiguration(TestConfiguration::class.java)
        assertEquals(testActionConfiguration.shopperLocale, deparcelizedActionConfig?.shopperLocale)
        assertEquals(testActionConfiguration.environment, deparcelizedActionConfig?.environment)
        assertEquals(testActionConfiguration.clientKey, deparcelizedActionConfig?.clientKey)
        assertEquals(testActionConfiguration.amount, deparcelizedActionConfig?.amount)
        assertEquals(testActionConfiguration.analyticsConfiguration, deparcelizedActionConfig?.analyticsConfiguration)
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val LIVE_CLIENT_KEY = "live_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
