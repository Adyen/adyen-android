/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class CashAppPayComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom configuration fields are null then all fields should match`() {
        val configuration = getConfigurationBuilder()
            .setReturnUrl(TEST_RETURN_URL)
            .build()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
        )

        val expected = getComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom configuration fields are set then all fields should match`() {
        val configuration = CashAppPayConfiguration.Builder(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2
        )
            .setCashAppPayEnvironment(CashAppPayEnvironment.PRODUCTION)
            .setReturnUrl("https://google.com")
            .setShowStorePaymentField(false)
            .setStorePaymentMethod(true)
            .setSubmitButtonVisible(false)
            .build()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
        )

        val expected = getComponentParams(
            isSubmitButtonVisible = false,
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            cashAppPayEnvironment = CashAppPayEnvironment.PRODUCTION,
            returnUrl = "https://google.com",
            showStorePaymentField = false,
            storePaymentMethod = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override custom configuration fields`() {
        val configuration = getConfigurationBuilder()
            .setReturnUrl(TEST_RETURN_URL)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L
            )
        )

        val params = CashAppPayComponentParamsMapper(overrideParams, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
        )

        val expected = getComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L
            )
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("enableStoreDetailsSource")
    fun `showStorePaymentField should match value set in sessions if it exists, otherwise should match configuration`(
        configurationValue: Boolean,
        sessionsValue: Boolean?,
        expectedValue: Boolean
    ) {
        val cardConfiguration = getConfigurationBuilder()
            .setReturnUrl(TEST_RETURN_URL)
            .setShowStorePaymentField(configurationValue)
            .build()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = cardConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = sessionsValue,
                installmentOptions = null,
                amount = null,
                returnUrl = TEST_RETURN_URL,
            ),
            paymentMethod = getDefaultPaymentMethod(),
        )

        val expected = getComponentParams(
            showStorePaymentField = expectedValue
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val cardConfiguration = getConfigurationBuilder()
            .setReturnUrl(TEST_RETURN_URL)
            .setAmount(configurationValue)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = dropInValue?.let { getComponentParams(amount = it) }

        val params = CashAppPayComponentParamsMapper(overrideParams, null).mapToParams(
            cardConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentOptions = null,
                amount = sessionsValue,
                returnUrl = TEST_RETURN_URL,
            ),
            getDefaultPaymentMethod(),
        )

        val expected = getComponentParams(
            amount = expectedValue
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when returnUrl is not set, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = getConfigurationBuilder()
                .build()

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = getDefaultPaymentMethod(),
            )
        }
    }

    @Test
    fun `when clientId is not available, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = getConfigurationBuilder()
                .setReturnUrl(TEST_RETURN_URL)
                .build()

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = PaymentMethod(
                    configuration = Configuration(clientId = null, scopeId = TEST_SCOPE_ID)
                ),
            )
        }
    }

    @Test
    fun `when scopeId is not available, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = getConfigurationBuilder()
                .setReturnUrl(TEST_RETURN_URL)
                .build()

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = PaymentMethod(
                    configuration = Configuration(clientId = TEST_CLIENT_ID, scopeId = null)
                ),
            )
        }
    }

    @Test
    fun `when StoredPaymentMethod is used, then clientId and scopeId should be null`() {
        val configuration = getConfigurationBuilder()
            .setReturnUrl(TEST_RETURN_URL)
            .build()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = StoredPaymentMethod(),
        )

        val expected = getComponentParams(
            clientId = null,
            scopeId = null,
        )

        assertEquals(expected, params)
    }

    @Suppress("LongParameterList")
    private fun getComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        isAnalyticsEnabled: Boolean = true,
        isCreatedByDropIn: Boolean = false,
        amount: Amount = Amount.EMPTY,
        isSubmitButtonVisible: Boolean = true,
        cashAppPayEnvironment: CashAppPayEnvironment = CashAppPayEnvironment.SANDBOX,
        returnUrl: String = TEST_RETURN_URL,
        showStorePaymentField: Boolean = true,
        storePaymentMethod: Boolean = false,
        clientId: String? = TEST_CLIENT_ID,
        scopeId: String? = TEST_SCOPE_ID,
    ) = CashAppPayComponentParams(
        isSubmitButtonVisible = isSubmitButtonVisible,
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        cashAppPayEnvironment = cashAppPayEnvironment,
        returnUrl = returnUrl,
        showStorePaymentField = showStorePaymentField,
        storePaymentMethod = storePaymentMethod,
        clientId = clientId,
        scopeId = scopeId,
    )

    private fun getConfigurationBuilder() = CashAppPayConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1
    )

    private fun getDefaultPaymentMethod() = PaymentMethod(
        configuration = Configuration(clientId = TEST_CLIENT_ID, scopeId = TEST_SCOPE_ID)
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val TEST_CLIENT_ID = "test_client_id"
        private const val TEST_SCOPE_ID = "test_scope_id"
        private const val TEST_RETURN_URL = "test_return_url"

        @JvmStatic
        fun enableStoreDetailsSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(false, false, false),
            arguments(false, true, true),
            arguments(true, false, false),
            arguments(true, true, true),
            arguments(false, null, false),
            arguments(true, null, true),
        )

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )
    }
}
