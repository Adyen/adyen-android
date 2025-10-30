/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.old

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.threeds2.customization.UiCustomization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class Adyen3DS2ConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            adyen3DS2 {
                val uiCustomization = UiCustomization().apply {
                    setToolbarTitle("title")
                }
                setUiCustomization(uiCustomization)
                setThreeDSRequestorAppURL("some url")
            }
        }

        val actual = checkoutConfiguration.getAdyen3DS2Configuration()

        val expected = Adyen3DS2Configuration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setUiCustomization(
                UiCustomization().apply {
                    setToolbarTitle("title")
                },
            )
            .setThreeDSRequestorAppURL("some url")
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(
            expected.uiCustomization?.toolbarCustomization?.headerText,
            actual?.uiCustomization?.toolbarCustomization?.headerText,
        )
        assertEquals(expected.threeDSRequestorAppURL, actual?.threeDSRequestorAppURL)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = Adyen3DS2Configuration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setUiCustomization(
                UiCustomization().apply {
                    setToolbarTitle("title")
                },
            )
            .setThreeDSRequestorAppURL("some url")
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

        val actual3DS2Config = actual.getAdyen3DS2Configuration()
        assertEquals(config.shopperLocale, actual3DS2Config?.shopperLocale)
        assertEquals(config.environment, actual3DS2Config?.environment)
        assertEquals(config.clientKey, actual3DS2Config?.clientKey)
        assertEquals(config.amount, actual3DS2Config?.amount)
        assertEquals(config.analyticsConfiguration, actual3DS2Config?.analyticsConfiguration)
        assertEquals(
            config.uiCustomization?.toolbarCustomization?.headerText,
            actual3DS2Config?.uiCustomization?.toolbarCustomization?.headerText,
        )
        assertEquals(config.threeDSRequestorAppURL, actual3DS2Config?.threeDSRequestorAppURL)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
