package com.adyen.checkout.bcmc

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class BcmcConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            bcmc {
                setHolderNameRequired(true)
                setShowStorePaymentField(true)
                setShopperReference("shopperReference")
                setSubmitButtonVisible(false)
            }
        }

        val actual = checkoutConfiguration.getBcmcConfiguration()

        val expected = BcmcConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setShopperReference("shopperReference")
            .setSubmitButtonVisible(false)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.isHolderNameRequired, actual?.isHolderNameRequired)
        assertEquals(expected.isStorePaymentFieldVisible, actual?.isStorePaymentFieldVisible)
        assertEquals(expected.shopperReference, actual?.shopperReference)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = BcmcConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setShopperReference("shopperReference")
            .setSubmitButtonVisible(false)
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

        val actualBcmcConfig = actual.getBcmcConfiguration()
        assertEquals(config.shopperLocale, actualBcmcConfig?.shopperLocale)
        assertEquals(config.environment, actualBcmcConfig?.environment)
        assertEquals(config.clientKey, actualBcmcConfig?.clientKey)
        assertEquals(config.amount, actualBcmcConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualBcmcConfig?.analyticsConfiguration)
        assertEquals(config.isHolderNameRequired, actualBcmcConfig?.isHolderNameRequired)
        assertEquals(config.isStorePaymentFieldVisible, actualBcmcConfig?.isStorePaymentFieldVisible)
        assertEquals(config.shopperReference, actualBcmcConfig?.shopperReference)
        assertEquals(config.isSubmitButtonVisible, actualBcmcConfig?.isSubmitButtonVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
