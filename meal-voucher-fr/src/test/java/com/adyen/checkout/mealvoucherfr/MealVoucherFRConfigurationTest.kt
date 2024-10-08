/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/8/2024.
 */

package com.adyen.checkout.mealvoucherfr

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class MealVoucherFRConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            mealVoucherFR {
                setSubmitButtonVisible(false)
                setSecurityCodeRequired(false)
            }
        }

        val actual = checkoutConfiguration.getMealVoucherFRConfiguration()

        val expected = MealVoucherFRConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSubmitButtonVisible(false)
            .setSecurityCodeRequired(false)
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.isSubmitButtonVisible, actual?.isSubmitButtonVisible)
        assertEquals(expected.isSecurityCodeRequired, actual?.isSecurityCodeRequired)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = MealVoucherFRConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setSubmitButtonVisible(false)
            .setSecurityCodeRequired(false)
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

        val actualMealVoucherFRConfig = actual.getMealVoucherFRConfiguration()
        assertEquals(config.shopperLocale, actualMealVoucherFRConfig?.shopperLocale)
        assertEquals(config.environment, actualMealVoucherFRConfig?.environment)
        assertEquals(config.clientKey, actualMealVoucherFRConfig?.clientKey)
        assertEquals(config.amount, actualMealVoucherFRConfig?.amount)
        assertEquals(config.analyticsConfiguration, actualMealVoucherFRConfig?.analyticsConfiguration)
        assertEquals(config.isSubmitButtonVisible, actualMealVoucherFRConfig?.isSubmitButtonVisible)
        assertEquals(config.isSecurityCodeRequired, actualMealVoucherFRConfig?.isSecurityCodeRequired)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
