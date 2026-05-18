/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/5/2026.
 */

package com.adyen.checkout.core.common.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.AnalyticsConfiguration
import com.adyen.checkout.core.components.AnalyticsLevel
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionSetupConfiguration
import com.adyen.checkout.core.sessions.SessionSetupInstallmentOptions
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CheckoutParamsFactoryTest {

    private val deviceLocale = Locale.GERMANY
    private val factory = CheckoutParamsFactory(deviceLocaleProvider = { deviceLocale })

    @Nested
    inner class ShopperLocaleTest {

        @Test
        fun `when configuration has shopperLocale, then use configuration locale`() {
            val configuration = createConfiguration(shopperLocale = Locale.FRANCE)
            val session = createSession(shopperLocale = "en-US")

            val result = factory.create(configuration, session, null)

            assertEquals(Locale.FRANCE, result.shopperLocale)
        }

        @Test
        fun `when configuration has no shopperLocale and session has valid locale, then use session locale`() {
            val configuration = createConfiguration(shopperLocale = null)
            val session = createSession(shopperLocale = "en-US")

            val result = factory.create(configuration, session, null)

            assertEquals(Locale.US, result.shopperLocale)
        }

        @Test
        fun `when configuration has no shopperLocale and session has no locale, then use device locale`() {
            val configuration = createConfiguration(shopperLocale = null)
            val session = createSession(shopperLocale = null)

            val result = factory.create(configuration, session, null)

            assertEquals(deviceLocale, result.shopperLocale)
        }

        @Test
        fun `when configuration has no shopperLocale and session is null, then use device locale`() {
            val configuration = createConfiguration(shopperLocale = null)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(deviceLocale, result.shopperLocale)
        }

        @Test
        fun `when configuration has no shopperLocale and session has locale, then session locale takes priority over device`() {
            val configuration = createConfiguration(shopperLocale = null)
            val session = createSession(shopperLocale = "nl-NL")

            val result = factory.create(configuration, session, null)

            assertEquals(Locale.forLanguageTag("nl-NL"), result.shopperLocale)
        }
    }

    @Nested
    inner class EnvironmentTest {

        @Test
        fun `when session is present, then use session environment`() {
            val configuration = createConfiguration(environment = Environment.TEST)
            val session = createSession(environment = Environment.LIVE_EUROPE)

            val result = factory.create(configuration, session, null)

            assertEquals(Environment.LIVE_EUROPE, result.environment)
        }

        @Test
        fun `when session is null, then use configuration environment`() {
            val configuration = createConfiguration(environment = Environment.TEST)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(Environment.TEST, result.environment)
        }
    }

    @Nested
    inner class ClientKeyTest {

        @Test
        fun `when session is present, then use session clientKey`() {
            val configuration = createConfiguration(clientKey = "test_config_key")
            val session = createSession(clientKey = "test_session_key")

            val result = factory.create(configuration, session, null)

            assertEquals("test_session_key", result.clientKey)
        }

        @Test
        fun `when session is null, then use configuration clientKey`() {
            val configuration = createConfiguration(clientKey = "test_config_key")

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals("test_config_key", result.clientKey)
        }
    }

    @Nested
    inner class AnalyticsParamsTest {

        @Test
        fun `when analyticsConfiguration is null, then level is ALL`() {
            val configuration = createConfiguration(analyticsConfiguration = null)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(AnalyticsParams(level = AnalyticsParamsLevel.ALL), result.analyticsParams)
        }

        @Test
        fun `when analyticsLevel is ALL, then level is ALL`() {
            val analyticsConfig = AnalyticsConfiguration(level = AnalyticsLevel.ALL)
            val configuration = createConfiguration(analyticsConfiguration = analyticsConfig)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(AnalyticsParams(level = AnalyticsParamsLevel.ALL), result.analyticsParams)
        }

        @Test
        fun `when analyticsLevel is NONE, then level is INITIAL`() {
            val analyticsConfig = AnalyticsConfiguration(level = AnalyticsLevel.NONE)
            val configuration = createConfiguration(analyticsConfiguration = analyticsConfig)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(AnalyticsParams(level = AnalyticsParamsLevel.INITIAL), result.analyticsParams)
        }
    }

    @Nested
    inner class AmountTest {

        @Test
        fun `when session has amount, then use session amount`() {
            val sessionAmount = Amount(currency = "EUR", value = 1000L)
            val configAmount = Amount(currency = "USD", value = 500L)
            val configuration = createConfiguration(amount = configAmount)
            val session = createSession(amount = sessionAmount)

            val result = factory.create(configuration, session, null)

            assertEquals(sessionAmount, result.amount)
        }

        @Test
        fun `when session has no amount, then use configuration amount`() {
            val configAmount = Amount(currency = "USD", value = 500L)
            val configuration = createConfiguration(amount = configAmount)
            val session = createSession(amount = null)

            val result = factory.create(configuration, session, null)

            assertEquals(configAmount, result.amount)
        }

        @Test
        fun `when session is null and configuration has no amount, then amount is null`() {
            val configuration = createConfiguration(amount = null)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertNull(result.amount)
        }
    }

    @Nested
    inner class ShowSubmitButtonTest {

        @Test
        fun `when configuration has showSubmitButton set, then use that value`() {
            val configuration = createConfiguration(showSubmitButton = false)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertEquals(false, result.showSubmitButton)
        }

        @Test
        fun `when configuration has showSubmitButton null, then default to true`() {
            val configuration = createConfiguration(showSubmitButton = null)

            val result = factory.create(configuration, session = null, publicKey = null)

            assertTrue(result.showSubmitButton)
        }
    }

    @Nested
    inner class PublicKeyTest {

        @Test
        fun `when publicKey is provided, then it is set`() {
            val configuration = createConfiguration()

            val result = factory.create(configuration, session = null, publicKey = "test_public_key")

            assertEquals("test_public_key", result.publicKey)
        }

        @Test
        fun `when publicKey is null, then it is null`() {
            val configuration = createConfiguration()

            val result = factory.create(configuration, session = null, publicKey = null)

            assertNull(result.publicKey)
        }
    }

    @Nested
    inner class AdditionalSessionParamsTest {

        @Test
        fun `when session is null, then additionalSessionParams is null`() {
            val configuration = createConfiguration()

            val result = factory.create(configuration, session = null, publicKey = null)

            assertNull(result.additionalSessionParams)
        }

        @Test
        fun `when session has configuration, then additionalSessionParams is created from it`() {
            val setupConfig = SessionSetupConfiguration(
                enableStoreDetails = true,
                showRemovePaymentMethodButton = false,
            )
            val configuration = createConfiguration()
            val session = createSession(
                sessionConfiguration = setupConfig,
                returnUrl = "test://return",
            )

            val result = factory.create(configuration, session, null)

            val expected = AdditionalSessionParams(
                enableStoreDetails = true,
                installmentConfiguration = SessionInstallmentConfiguration(
                    installmentOptions = null,
                    showInstallmentAmount = false,
                ),
                showRemovePaymentMethodButton = false,
                returnUrl = "test://return",
            )
            assertEquals(expected, result.additionalSessionParams)
        }

        @Test
        fun `when session has installment options, then they are mapped correctly`() {
            val installmentOptions = mapOf(
                "card" to SessionSetupInstallmentOptions(
                    plans = listOf("regular"),
                    preselectedValue = 3,
                    values = listOf(2, 3, 6),
                ),
            )
            val setupConfig = SessionSetupConfiguration(
                installmentOptions = installmentOptions,
                showInstallmentAmount = true,
            )
            val configuration = createConfiguration()
            val session = createSession(sessionConfiguration = setupConfig)

            val result = factory.create(configuration, session, null)

            val expectedInstallmentConfig = SessionInstallmentConfiguration(
                installmentOptions = mapOf(
                    "card" to SessionInstallmentOptionsParams(
                        plans = listOf("regular"),
                        preselectedValue = 3,
                        values = listOf(2, 3, 6),
                    ),
                ),
                showInstallmentAmount = true,
            )
            assertEquals(expectedInstallmentConfig, result.additionalSessionParams?.installmentConfiguration)
        }

        @Test
        fun `when session has null configuration, then additionalSessionParams has null fields`() {
            val configuration = createConfiguration()
            val session = createSession(sessionConfiguration = null)

            val result = factory.create(configuration, session, null)

            val expected = AdditionalSessionParams(
                enableStoreDetails = null,
                installmentConfiguration = SessionInstallmentConfiguration(
                    installmentOptions = null,
                    showInstallmentAmount = null,
                ),
                showRemovePaymentMethodButton = null,
                returnUrl = null,
            )
            assertEquals(expected, result.additionalSessionParams)
        }
    }

    @Suppress("LongParameterList")
    private fun createConfiguration(
        environment: Environment = Environment.TEST,
        clientKey: String = "test_qwertyuiopasdfgh",
        shopperLocale: Locale? = null,
        amount: Amount? = null,
        analyticsConfiguration: AnalyticsConfiguration? = null,
        showSubmitButton: Boolean? = null,
    ) = CheckoutConfiguration(
        environment = environment,
        clientKey = clientKey,
        shopperLocale = shopperLocale,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        showSubmitButton = showSubmitButton,
    )

    @Suppress("LongParameterList")
    private fun createSession(
        environment: Environment = Environment.LIVE_EUROPE,
        clientKey: String = "live_qwertyuiopasdfgh",
        shopperLocale: String? = null,
        amount: Amount? = null,
        sessionConfiguration: SessionSetupConfiguration? = null,
        returnUrl: String? = null,
    ) = CheckoutSession(
        sessionSetupResponse = SessionSetupResponse(
            id = "test_session_id",
            sessionData = "test_session_data",
            amount = amount,
            expiresAt = "2026-12-31T23:59:59Z",
            paymentMethods = null,
            returnUrl = returnUrl,
            configuration = sessionConfiguration,
            shopperLocale = shopperLocale,
        ),
        order = null,
        environment = environment,
        clientKey = clientKey,
    )
}
