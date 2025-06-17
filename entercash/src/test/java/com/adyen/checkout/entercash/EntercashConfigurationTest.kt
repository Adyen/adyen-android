package com.adyen.checkout.entercash

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class EntercashConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            entercash {
                setViewType(IssuerListViewType.SPINNER_VIEW)
                setHideIssuerLogos(true)
                setSubmitButtonVisible(false)
            }
        }

        val actual = checkoutConfiguration.getEntercashConfiguration()

        val expected = EntercashConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .setHideIssuerLogos(true)
            .setSubmitButtonVisible(false)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.viewType, actual?.viewType)
        assertEquals(expected.hideIssuerLogos, actual?.hideIssuerLogos)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = EntercashConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .setHideIssuerLogos(true)
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

        val actualEnterConfig = actual.getEntercashConfiguration()
        assertEquals(config.shopperLocale, actualEnterConfig?.shopperLocale)
        assertEquals(config.environment, actualEnterConfig?.environment)
        assertEquals(config.clientKey, actualEnterConfig?.clientKey)
        assertEquals(config.amount, actualEnterConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualEnterConfig?.analyticsConfiguration)
        assertEquals(config.viewType, actualEnterConfig?.viewType)
        assertEquals(config.hideIssuerLogos, actualEnterConfig?.hideIssuerLogos)
        assertEquals(config.isSubmitButtonVisible, actualEnterConfig?.isSubmitButtonVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
