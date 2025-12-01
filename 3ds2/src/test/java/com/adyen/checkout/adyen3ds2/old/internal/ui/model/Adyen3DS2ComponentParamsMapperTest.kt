/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.old.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.threeds2.old.adyen3DS2
import com.adyen.checkout.threeds2.old.internal.ui.model.Adyen3DS2ComponentParams
import com.adyen.checkout.threeds2.old.internal.ui.model.Adyen3DS2ComponentParamsMapper
import com.adyen.threeds2.customization.UiCustomization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class Adyen3DS2ComponentParamsMapperTest {

    private val adyen3DS2ComponentParamsMapper =
        Adyen3DS2ComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom 3ds2 configuration fields are null then all fields should match`() {
        val checkoutConfiguration = getCheckoutConfiguration()

        val params = adyen3DS2ComponentParamsMapper.mapToParams(checkoutConfiguration, DEVICE_LOCALE, null, null)

        val expected = getAdyen3DS2ComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom 3ds2 configuration fields are set then all fields should match`() {
        val uiCustomization = UiCustomization()

        val testUrl = "https://adyen.com"
        val configuration = getCheckoutConfiguration {
            adyen3DS2 {
                setUiCustomization(uiCustomization)
                setThreeDSRequestorAppURL(testUrl)
            }
        }

        val params = adyen3DS2ComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getAdyen3DS2ComponentParams(
            uiCustomization = uiCustomization,
            threeDSRequestorAppURL = testUrl,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override 3ds2 configuration fields`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
            amount = Amount(
                currency = "USD",
                value = 25_00L,
            ),
        )

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = adyen3DS2ComponentParamsMapper.mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
        )

        val expected = getAdyen3DS2ComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.INITIAL, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
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
        val configuration = getCheckoutConfiguration(amount = configurationValue)

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val params = adyen3DS2ComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
        )

        val expected = getAdyen3DS2ComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `shopper locale should match value set in configuration then sessions then device locale`(
        configurationValue: Locale?,
        sessionsValue: Locale?,
        deviceLocaleValue: Locale,
        expectedValue: Locale,
    ) {
        val configuration = getCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = createSessionParams(
            shopperLocale = sessionsValue,
        )

        val params = adyen3DS2ComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getAdyen3DS2ComponentParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `environment and client key should match value set in sessions`() {
        val configuration = getCheckoutConfiguration()

        val sessionParams = createSessionParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        val params = adyen3DS2ComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getAdyen3DS2ComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    private fun getCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        config: CheckoutConfiguration.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
        configurationBlock = config,
    )

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

    @Suppress("LongParameterList")
    private fun getAdyen3DS2ComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        uiCustomization: UiCustomization? = null,
        threeDSRequestorAppURL: String? = null,
    ) = Adyen3DS2ComponentParams(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        uiCustomization = uiCustomization,
        threeDSRequestorAppURL = threeDSRequestorAppURL,
        deviceParameterBlockList = Adyen3DS2ComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            Arguments.arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            Arguments.arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            Arguments.arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            // configurationValue, sessionsValue, deviceLocaleValue, expectedValue
            Arguments.arguments(null, null, Locale.US, Locale.US),
            Arguments.arguments(Locale.GERMAN, null, Locale.US, Locale.GERMAN),
            Arguments.arguments(null, Locale.CHINESE, Locale.US, Locale.CHINESE),
            Arguments.arguments(Locale.GERMAN, Locale.CHINESE, Locale.US, Locale.GERMAN),
        )
    }
}
