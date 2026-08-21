/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/8/2026.
 */

package com.adyen.checkout.core.sessions.internal.model

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionSetupConfiguration
import com.adyen.checkout.core.sessions.SessionSetupInstallmentOptions
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetails
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class SessionParamsFactoryTest {

    @Nested
    inner class FromCheckoutSessionTest {

        @Test
        fun `when creating from a session, then every value is taken from the session`() {
            val session = createSession(
                sessionConfiguration = SessionSetupConfiguration(
                    enableStoreDetails = true,
                    showInstallmentAmount = true,
                    showRemovePaymentMethodButton = false,
                ),
            )

            val result = SessionParamsFactory.create(session)

            val expected = SessionParams(
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY,
                enableStoreDetails = true,
                installmentConfiguration = SessionInstallmentConfiguration(
                    installmentOptions = null,
                    showInstallmentAmount = true,
                ),
                showRemovePaymentMethodButton = false,
                amount = TEST_AMOUNT,
                returnUrl = TEST_RETURN_URL,
                shopperLocale = Locale.US,
            )
            assertEquals(expected, result)
        }

        @Test
        fun `when the session has no setup configuration, then the derived values are null`() {
            val session = createSession(sessionConfiguration = null)

            val result = SessionParamsFactory.create(session)

            assertNull(result.enableStoreDetails)
            assertNull(result.showRemovePaymentMethodButton)
            assertEquals(
                SessionInstallmentConfiguration(installmentOptions = null, showInstallmentAmount = null),
                result.installmentConfiguration,
            )
        }
    }

    @Nested
    inner class FromSessionDetailsTest {

        @Test
        fun `when creating from session details, then every value is taken from the details`() {
            val sessionDetails = createSessionDetails(
                sessionSetupConfiguration = SessionSetupConfiguration(
                    enableStoreDetails = false,
                    showInstallmentAmount = false,
                    showRemovePaymentMethodButton = true,
                ),
            )

            val result = SessionParamsFactory.create(sessionDetails)

            val expected = SessionParams(
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY,
                enableStoreDetails = false,
                installmentConfiguration = SessionInstallmentConfiguration(
                    installmentOptions = null,
                    showInstallmentAmount = false,
                ),
                showRemovePaymentMethodButton = true,
                amount = TEST_AMOUNT,
                returnUrl = TEST_RETURN_URL,
                shopperLocale = Locale.US,
            )
            assertEquals(expected, result)
        }

        @Test
        fun `when the details have no setup configuration, then the derived values are null`() {
            val sessionDetails = createSessionDetails(sessionSetupConfiguration = null)

            val result = SessionParamsFactory.create(sessionDetails)

            assertNull(result.enableStoreDetails)
            assertNull(result.showRemovePaymentMethodButton)
            assertEquals(
                SessionInstallmentConfiguration(installmentOptions = null, showInstallmentAmount = null),
                result.installmentConfiguration,
            )
        }
    }

    @Nested
    inner class InstallmentOptionsTest {

        @Test
        fun `when the session has installment options, then they are mapped per payment method`() {
            val session = createSession(
                sessionConfiguration = SessionSetupConfiguration(
                    showInstallmentAmount = true,
                    installmentOptions = mapOf(
                        "card" to SessionSetupInstallmentOptions(
                            plans = listOf("regular"),
                            preselectedValue = 3,
                            values = listOf(2, 3, 6),
                        ),
                    ),
                ),
            )

            val result = SessionParamsFactory.create(session)

            val expected = SessionInstallmentConfiguration(
                installmentOptions = mapOf(
                    "card" to SessionInstallmentOptionsParams(
                        plans = listOf("regular"),
                        preselectedValue = 3,
                        values = listOf(2, 3, 6),
                    ),
                ),
                showInstallmentAmount = true,
            )
            assertEquals(expected, result.installmentConfiguration)
        }

        @Test
        fun `when an installment option is null, then the key is kept with a null value`() {
            val session = createSession(
                sessionConfiguration = SessionSetupConfiguration(
                    installmentOptions = mapOf("card" to null),
                ),
            )

            val result = SessionParamsFactory.create(session)

            assertEquals(mapOf("card" to null), result.installmentConfiguration?.installmentOptions)
        }
    }

    @Nested
    inner class ShopperLocaleTest {

        @Test
        fun `when the shopper locale is a valid language tag, then it is parsed`() {
            val session = createSession(shopperLocale = "nl-NL")

            val result = SessionParamsFactory.create(session)

            assertEquals(Locale.forLanguageTag("nl-NL"), result.shopperLocale)
        }

        @Test
        fun `when there is no shopper locale, then the shopper locale is null`() {
            val session = createSession(shopperLocale = null)

            val result = SessionParamsFactory.create(session)

            assertNull(result.shopperLocale)
        }

        @Test
        fun `when the shopper locale is malformed, then the root locale is returned`() {
            // Documents current behaviour rather than intended behaviour: Locale.forLanguageTag does not throw
            // for malformed tags, it returns the root locale, so the runCatching fallback in
            // SessionParamsFactory is unreachable and the malformed locale is never turned into null.
            val session = createSession(shopperLocale = "not a language tag")

            val result = SessionParamsFactory.create(session)

            assertEquals(Locale.ROOT, result.shopperLocale)
        }
    }

    private fun createSession(
        shopperLocale: String? = "en-US",
        sessionConfiguration: SessionSetupConfiguration? = null,
    ) = CheckoutSession(
        sessionSetupResponse = SessionSetupResponse(
            id = TEST_SESSION_ID,
            sessionData = TEST_SESSION_DATA,
            amount = TEST_AMOUNT,
            expiresAt = TEST_EXPIRES_AT,
            paymentMethods = null,
            returnUrl = TEST_RETURN_URL,
            configuration = sessionConfiguration,
            shopperLocale = shopperLocale,
        ),
        order = null,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    private fun createSessionDetails(
        shopperLocale: String? = "en-US",
        sessionSetupConfiguration: SessionSetupConfiguration? = null,
    ) = SessionDetails(
        id = TEST_SESSION_ID,
        sessionData = TEST_SESSION_DATA,
        amount = TEST_AMOUNT,
        expiresAt = TEST_EXPIRES_AT,
        returnUrl = TEST_RETURN_URL,
        sessionSetupConfiguration = sessionSetupConfiguration,
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    companion object {
        private const val TEST_SESSION_ID = "test_session_id"
        private const val TEST_SESSION_DATA = "test_session_data"
        private const val TEST_EXPIRES_AT = "2026-12-31T23:59:59Z"
        private const val TEST_RETURN_URL = "test://return"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1000L)
    }
}
