package com.adyen.checkout.ach

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class ACHDirectDebitConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            achDirectDebit {
                setSubmitButtonVisible(false)
                setAddressConfiguration(ACHDirectDebitAddressConfiguration.FullAddress(listOf("test")))
                setShowStorePaymentField(true)
            }
        }

        val actual = checkoutConfiguration.getACHDirectDebitConfiguration()

        val expected = ACHDirectDebitConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSubmitButtonVisible(false)
            .setAddressConfiguration(ACHDirectDebitAddressConfiguration.FullAddress(listOf("test")))
            .setShowStorePaymentField(true)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
        assertEquals(expected.addressConfiguration, actual?.addressConfiguration)
        assertEquals(expected.isStorePaymentFieldVisible, actual?.isStorePaymentFieldVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = ACHDirectDebitConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSubmitButtonVisible(false)
            .setAddressConfiguration(ACHDirectDebitAddressConfiguration.FullAddress(listOf("test")))
            .setShowStorePaymentField(true)
            .build()

        val actual = config.toCheckoutConfiguration()

        val expected = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        )

        assertEquals(expected.shopperLocale, actual.shopperLocale)
        assertEquals(expected.environment, actual.environment)
        assertEquals(expected.clientKey, actual.clientKey)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.analyticsConfiguration, actual.analyticsConfiguration)

        val actualACHConfig = actual.getACHDirectDebitConfiguration()
        assertEquals(config.shopperLocale, actualACHConfig?.shopperLocale)
        assertEquals(config.environment, actualACHConfig?.environment)
        assertEquals(config.clientKey, actualACHConfig?.clientKey)
        assertEquals(config.amount, actualACHConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualACHConfig?.analyticsConfiguration)
        assertEquals(config.isSubmitButtonVisible, actualACHConfig?.isSubmitButtonVisible)
        assertEquals(config.addressConfiguration, actualACHConfig?.addressConfiguration)
        assertEquals(config.isStorePaymentFieldVisible, actualACHConfig?.isStorePaymentFieldVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
