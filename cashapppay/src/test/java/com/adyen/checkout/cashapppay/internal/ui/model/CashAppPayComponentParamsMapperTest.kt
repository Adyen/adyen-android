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
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
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

    private val cashAppPayComponentParamsMapper = CashAppPayComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom configuration fields are set then all fields should match`() {
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

        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            isSubmitButtonVisible = false,
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_2),
            cashAppPayEnvironment = CashAppPayEnvironment.PRODUCTION,
            returnUrl = "https://google.com",
            showStorePaymentField = false,
            storePaymentMethod = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override custom configuration fields`() {
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

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params =
            cashAppPayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                paymentMethod = getDefaultPaymentMethod(),
                context = Application(),
            )

        val expected = getComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            cashAppPayEnvironment = CashAppPayEnvironment.PRODUCTION,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in cash app configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
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
                setSubmitButtonVisible(false)
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @ParameterizedTest
    @MethodSource("enableStoreDetailsSource")
    fun `showStorePaymentField should match value set in sessions if it exists, otherwise should match configuration`(
        configurationValue: Boolean,
        sessionsValue: Boolean?,
        expectedValue: Boolean
    ) {
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(configurationValue)
        }
        val sessionParams = createSessionParams(
            enableStoreDetails = sessionsValue,
            returnUrl = TEST_RETURN_URL,
        )
        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
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
    fun `amount should match value set in sessions then drop in then component configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }
        val sessionParams = createSessionParams(
            amount = sessionsValue,
            returnUrl = TEST_RETURN_URL,
        )
        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
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

            cashAppPayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
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
        val sessionParams = createSessionParams(
            enableStoreDetails = false,
            returnUrl = "sessionReturnUrl",
        )
        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        assertEquals("sessionReturnUrl", params.returnUrl)
    }

    @Test
    fun `when clientId is not available, then an exception is thrown`() {
        assertThrows<ComponentException> {
            val configuration = createCheckoutConfiguration()

            cashAppPayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
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

            cashAppPayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
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

        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            storedPaymentMethod = StoredPaymentMethod(),
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
            DropInOverrideParams(Amount("CAD", 123L), null)
        } else {
            null
        }
        val params =
            cashAppPayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                storedPaymentMethod = StoredPaymentMethod(),
                context = mockContext,
            )

        assertEquals(expected, params.returnUrl)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `shopper locale should match value set in configuration then sessions then device locale`(
        configurationValue: Locale?,
        sessionsValue: Locale?,
        deviceLocaleValue: Locale,
        expectedValue: Locale,
    ) {
        val configuration = createCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = createSessionParams(
            returnUrl = TEST_RETURN_URL,
            shopperLocale = sessionsValue,
        )

        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `environment and client key should match value set in sessions`() {
        val configuration = createCheckoutConfiguration()

        val sessionParams = createSessionParams(
            returnUrl = TEST_RETURN_URL,
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        val params = cashAppPayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = getDefaultPaymentMethod(),
            context = Application(),
        )

        val expected = getComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
            cashAppPayEnvironment = CashAppPayEnvironment.PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Suppress("LongParameterList")
    private fun getComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
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
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        isSubmitButtonVisible = isSubmitButtonVisible,
        cashAppPayEnvironment = cashAppPayEnvironment,
        returnUrl = returnUrl,
        showStorePaymentField = showStorePaymentField,
        storePaymentMethod = storePaymentMethod,
        clientId = clientId,
        scopeId = scopeId,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: CashAppPayConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        cashAppPay {
            setReturnUrl(TEST_RETURN_URL)
            apply(configuration)
        }
    }

    @Suppress("LongParameterList")
    private fun createSessionParams(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        enableStoreDetails: Boolean? = null,
        installmentConfiguration: SessionInstallmentConfiguration? = null,
        showRemovePaymentMethodButton: Boolean? = null,
        amount: Amount? = null,
        returnUrl: String? = "",
        shopperLocale: Locale? = null,
    ) = SessionParams(
        environment = environment,
        clientKey = clientKey,
        enableStoreDetails = enableStoreDetails,
        installmentConfiguration = installmentConfiguration,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
        amount = amount,
        returnUrl = returnUrl,
        shopperLocale = shopperLocale,
    )

    private fun getDefaultPaymentMethod() = PaymentMethod(
        configuration = Configuration(clientId = TEST_CLIENT_ID, scopeId = TEST_SCOPE_ID),
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val TEST_CLIENT_ID = "test_client_id"
        private const val TEST_SCOPE_ID = "test_scope_id"
        private const val TEST_RETURN_URL = "test_return_url"
        private val DEVICE_LOCALE = Locale("nl", "NL")

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

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            // configurationValue, sessionsValue, deviceLocaleValue, expectedValue
            arguments(null, null, Locale.US, Locale.US),
            arguments(Locale.GERMAN, null, Locale.US, Locale.GERMAN),
            arguments(null, Locale.CHINESE, Locale.US, Locale.CHINESE),
            arguments(Locale.GERMAN, Locale.CHINESE, Locale.US, Locale.GERMAN),
        )
    }
}
