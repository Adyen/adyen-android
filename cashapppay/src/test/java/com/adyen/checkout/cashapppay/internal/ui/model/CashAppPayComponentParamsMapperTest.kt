/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import android.app.Application
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.cashapppay.cashAppPay
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Locale

internal class CashAppPayComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom configuration fields are set then all fields should match`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            cashAppPay {
                setCashAppPayEnvironment(CashAppPayEnvironment.PRODUCTION)
                setReturnUrl("https://google.com")
                setShowStorePaymentField(false)
                setStorePaymentMethod(true)
                setSubmitButtonVisible(false)
            }
        }

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
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
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            cashAppPay {
                setReturnUrl(TEST_RETURN_URL)
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L))
        val params = CashAppPayComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            cashAppPayEnvironment = CashAppPayEnvironment.PRODUCTION,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 123L,
            ),
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
        val cardConfiguration = createCheckoutConfiguration {
            setShowStorePaymentField(configurationValue)
        }

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = cardConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = sessionsValue,
                installmentConfiguration = null,
                amount = null,
                returnUrl = TEST_RETURN_URL,
            ),
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            showStorePaymentField = expectedValue,
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
        val cardConfiguration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it) }

        val params = CashAppPayComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            configuration = cardConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = TEST_RETURN_URL,
            ),
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when returnUrl is not set, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = CheckoutConfiguration(
                environment = Environment.TEST,
                shopperLocale = Locale.US,
                clientKey = TEST_CLIENT_KEY_1,
            ) {
                cashAppPay()
            }

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = getDefaultPaymentMethod(),
                context = Application(),
            )
        }
    }

    @Test
    fun `when returnUrl is not set and session params are provided, then the return url from sessions should be used`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            shopperLocale = Locale.US,
            clientKey = TEST_CLIENT_KEY_1,
        ) {
            cashAppPay()
        }

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = SessionParams(false, null, null, "sessionReturnUrl"),
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        assertEquals("sessionReturnUrl", params.returnUrl)
    }

    @Test
    fun `when clientId is not available, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = createCheckoutConfiguration()

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = PaymentMethod(
                    configuration = Configuration(clientId = null, scopeId = TEST_SCOPE_ID),
                ),
                context = Application(),
            )
        }
    }

    @Test
    fun `when scopeId is not available, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = createCheckoutConfiguration()

            CashAppPayComponentParamsMapper(null, null).mapToParams(
                configuration = configuration,
                sessionParams = null,
                paymentMethod = PaymentMethod(
                    configuration = Configuration(clientId = TEST_CLIENT_ID, scopeId = null),
                ),
                context = Application(),
            )
        }
    }

    @Test
    fun `when StoredPaymentMethod is used, then clientId and scopeId should be null`() {
        val configuration = createCheckoutConfiguration()

        val params = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = StoredPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            clientId = null,
            scopeId = null,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("returnUrlSource")
    fun `when returnUrl and is created by drop-in, then expect`(
        returnUrl: String?,
        isCreatedByDropIn: Boolean,
        expected: String?,
    ) {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
        ) {
            cashAppPay {
                returnUrl?.let { setReturnUrl(it) }
            }
        }

        val mockContext = mock<Application>()
        whenever(mockContext.packageName) doReturn "com.test.test"
        val dropInOverrideParams = if (isCreatedByDropIn) {
            DropInOverrideParams(Amount("CAD", 123L))
        } else {
            null
        }
        val params = CashAppPayComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = StoredPaymentMethod(),
            context = mockContext,
        )

        assertEquals(expected, params.returnUrl)
    }

    @Suppress("LongParameterList")
    private fun getComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
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
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        cashAppPayEnvironment = cashAppPayEnvironment,
        returnUrl = returnUrl,
        showStorePaymentField = showStorePaymentField,
        storePaymentMethod = storePaymentMethod,
        clientId = clientId,
        scopeId = scopeId,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: CashAppPayConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        cashAppPay {
            setReturnUrl(TEST_RETURN_URL)
            apply(configuration)
        }
    }

    private fun getDefaultPaymentMethod() = PaymentMethod(
        configuration = Configuration(clientId = TEST_CLIENT_ID, scopeId = TEST_SCOPE_ID),
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

        @JvmStatic
        fun returnUrlSource() = listOf(
            // Configured URL, isCreatedByDropIn, Expected
            arguments(TEST_RETURN_URL, false, TEST_RETURN_URL),
            arguments(null, false, null),
            arguments(null, true, "adyencheckout://com.test.test"),
        )
    }
}
