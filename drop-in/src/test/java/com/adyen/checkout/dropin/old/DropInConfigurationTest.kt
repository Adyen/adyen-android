/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old

import com.adyen.checkout.card.old.CardConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.googlepay.old.GooglePayComponent
import com.adyen.checkout.googlepay.old.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.redirect.old.RedirectConfiguration
import com.adyen.checkout.threeds2.old.Adyen3DS2Configuration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Locale

internal class DropInConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            dropIn {
                setShowPreselectedStoredPaymentMethod(false)
                setSkipListWhenSinglePaymentMethod(true)
                setEnableRemovingStoredPaymentMethods(true)
                overridePaymentMethodName("mc", "MC")
            }
        }

        val actual = checkoutConfiguration.getDropInConfiguration()

        val expected = DropInConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setShowPreselectedStoredPaymentMethod(false)
            .setSkipListWhenSinglePaymentMethod(true)
            .setEnableRemovingStoredPaymentMethods(true)
            .overridePaymentMethodName("mc", "MC")
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.showPreselectedStoredPaymentMethod, actual?.showPreselectedStoredPaymentMethod)
        assertEquals(expected.skipListWhenSinglePaymentMethod, actual?.skipListWhenSinglePaymentMethod)
        assertEquals(expected.isRemovingStoredPaymentMethodsEnabled, actual?.isRemovingStoredPaymentMethodsEnabled)
        assertEquals(expected.overriddenPaymentMethodInformation, actual?.overriddenPaymentMethodInformation)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = DropInConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setShowPreselectedStoredPaymentMethod(false)
            .setSkipListWhenSinglePaymentMethod(true)
            .setEnableRemovingStoredPaymentMethods(true)
            .overridePaymentMethodName("mc", "MC")
            .addCardConfiguration(CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build())
            .addGooglePayConfiguration(
                GooglePayConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addIdealConfiguration(IdealConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build())
            .add3ds2ActionConfiguration(
                Adyen3DS2Configuration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addRedirectActionConfiguration(
                RedirectConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build(),
            )
            .addWeChatPayActionConfiguration(
                WeChatPayActionConfiguration.Builder(
                    Locale.US,
                    Environment.TEST,
                    TEST_CLIENT_KEY,
                ).build(),
            )
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

        val actualDropInConfig = actual.getDropInConfiguration()
        assertEquals(config.shopperLocale, actualDropInConfig?.shopperLocale)
        assertEquals(config.environment, actualDropInConfig?.environment)
        assertEquals(config.clientKey, actualDropInConfig?.clientKey)
        assertEquals(config.amount, actualDropInConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualDropInConfig?.analyticsConfiguration)
        assertEquals(config.showPreselectedStoredPaymentMethod, actualDropInConfig?.showPreselectedStoredPaymentMethod)
        assertEquals(config.skipListWhenSinglePaymentMethod, actualDropInConfig?.skipListWhenSinglePaymentMethod)
        assertEquals(
            config.isRemovingStoredPaymentMethodsEnabled,
            actualDropInConfig?.isRemovingStoredPaymentMethodsEnabled,
        )
        assertEquals(config.overriddenPaymentMethodInformation, actualDropInConfig?.overriddenPaymentMethodInformation)
        assertNotNull(actual.getConfiguration(PaymentMethodTypes.SCHEME))
        assertNotNull(
            GooglePayComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key -> actual.getConfiguration(key) },
        )
        assertNotNull(actual.getConfiguration(PaymentMethodTypes.IDEAL))
        assertNotNull(actual.getActionConfiguration(Adyen3DS2Configuration::class.java))
        assertNotNull(actual.getActionConfiguration(RedirectConfiguration::class.java))
        assertNotNull(actual.getActionConfiguration(WeChatPayActionConfiguration::class.java))
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
