package com.adyen.checkout.cashapppay

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CashAppPayConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            cashAppPay {
                setCashAppPayEnvironment(CashAppPayEnvironment.PRODUCTION)
                setReturnUrl("some url")
                setShowStorePaymentField(true)
                setStorePaymentMethod(true)
                setSubmitButtonVisible(false)
            }
        }

        val actual = checkoutConfiguration.getCashAppPayConfiguration()

        val expected = CashAppPayConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setCashAppPayEnvironment(CashAppPayEnvironment.PRODUCTION)
            .setReturnUrl("some url")
            .setShowStorePaymentField(true)
            .setStorePaymentMethod(true)
            .setSubmitButtonVisible(false)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.cashAppPayEnvironment, actual?.cashAppPayEnvironment)
        assertEquals(expected.returnUrl, actual?.returnUrl)
        assertEquals(expected.showStorePaymentField, actual?.showStorePaymentField)
        assertEquals(expected.storePaymentMethod, actual?.storePaymentMethod)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = CashAppPayConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setCashAppPayEnvironment(CashAppPayEnvironment.PRODUCTION)
            .setReturnUrl("some url")
            .setShowStorePaymentField(true)
            .setStorePaymentMethod(true)
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

        val actualCashAppConfig = actual.getCashAppPayConfiguration()
        assertEquals(config.shopperLocale, actualCashAppConfig?.shopperLocale)
        assertEquals(config.environment, actualCashAppConfig?.environment)
        assertEquals(config.clientKey, actualCashAppConfig?.clientKey)
        assertEquals(config.amount, actualCashAppConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualCashAppConfig?.analyticsConfiguration)
        assertEquals(config.cashAppPayEnvironment, actualCashAppConfig?.cashAppPayEnvironment)
        assertEquals(config.returnUrl, actualCashAppConfig?.returnUrl)
        assertEquals(config.showStorePaymentField, actualCashAppConfig?.showStorePaymentField)
        assertEquals(config.storePaymentMethod, actualCashAppConfig?.storePaymentMethod)
        assertEquals(config.isSubmitButtonVisible, actualCashAppConfig?.isSubmitButtonVisible)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
