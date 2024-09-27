package com.adyen.checkout.twint

import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class TwintConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            twint {
                setShowStorePaymentField(true)
                setSubmitButtonVisible(false)
                setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
            }
        }

        val actual = checkoutConfiguration.getTwintConfiguration()

        val expected = TwintConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setShowStorePaymentField(true)
            .setSubmitButtonVisible(false)
            .setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.showStorePaymentField, actual?.showStorePaymentField)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = TwintConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setShowStorePaymentField(true)
            .setSubmitButtonVisible(false)
            .setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
            .build()

        val actual = config.toCheckoutConfiguration()

        val expected = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            twint {
                setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
            }
        }

        assertEquals(expected.shopperLocale, actual.shopperLocale)
        assertEquals(expected.environment, actual.environment)
        assertEquals(expected.clientKey, actual.clientKey)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.analyticsConfiguration, actual.analyticsConfiguration)

        val actualTwintConfig = actual.getTwintConfiguration()
        assertEquals(config.shopperLocale, actualTwintConfig?.shopperLocale)
        assertEquals(config.environment, actualTwintConfig?.environment)
        assertEquals(config.clientKey, actualTwintConfig?.clientKey)
        assertEquals(config.amount, actualTwintConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualTwintConfig?.analyticsConfiguration)
        assertEquals(config.showStorePaymentField, actualTwintConfig?.showStorePaymentField)
        assertEquals(config.isSubmitButtonVisible, actualTwintConfig?.isSubmitButtonVisible)
        assertEquals(config.actionHandlingMethod, actualTwintConfig?.actionHandlingMethod)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
