package com.adyen.checkout.openbanking

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class OpenBankingConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            openBanking {
                setViewType(IssuerListViewType.SPINNER_VIEW)
                setHideIssuerLogos(true)
                setSubmitButtonVisible(false)
            }
        }

        val actual = checkoutConfiguration.getOpenBankingConfiguration()

        val expected = OpenBankingConfiguration.Builder(
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
        val config = OpenBankingConfiguration.Builder(
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

        val actualOpenBankingConfig = actual.getOpenBankingConfiguration()
        assertEquals(config.shopperLocale, actualOpenBankingConfig?.shopperLocale)
        assertEquals(config.environment, actualOpenBankingConfig?.environment)
        assertEquals(config.clientKey, actualOpenBankingConfig?.clientKey)
        assertEquals(config.amount, actualOpenBankingConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualOpenBankingConfig?.analyticsConfiguration)
        assertEquals(config.viewType, actualOpenBankingConfig?.viewType)
        assertEquals(config.hideIssuerLogos, actualOpenBankingConfig?.hideIssuerLogos)
        assertEquals(config.isSubmitButtonVisible, actualOpenBankingConfig?.isSubmitButtonVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
