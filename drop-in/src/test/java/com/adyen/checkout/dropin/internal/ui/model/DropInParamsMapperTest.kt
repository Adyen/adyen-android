/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/2/2024.
 */

package com.adyen.checkout.dropin.internal.ui.model

import android.os.Bundle
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.dropIn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class DropInParamsMapperTest {

    private val dropInParamsMapper = DropInParamsMapper()

    @Test
    fun `when created without specific drop-in or checkout configurations, then params should have default values`() {
        val configuration = createCheckoutConfiguration()

        val params = dropInParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null)

        val expected = getDropInParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when specific drop-in configurations are set then params should match these values`() {
        val additionalData = Bundle().apply {
            putString("key", "value")
        }
        val overridePaymentMethod = "TYPE" to "NAME"
        val configuration = createCheckoutConfiguration {
            setShowPreselectedStoredPaymentMethod(false)
            setSkipListWhenSinglePaymentMethod(true)
            setEnableRemovingStoredPaymentMethods(true)
            setAdditionalDataForDropInService(additionalData)
            overridePaymentMethodName(overridePaymentMethod.first, overridePaymentMethod.second)
        }

        val params = dropInParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null)

        val expectedOverriddenPaymentMethodInformation = with(overridePaymentMethod) {
            hashMapOf(first to DropInPaymentMethodInformation(second))
        }
        val expected = getDropInParams(
            showPreselectedStoredPaymentMethod = false,
            skipListWhenSinglePaymentMethod = true,
            isRemovingStoredPaymentMethodsEnabled = true,
            additionalDataForDropInService = additionalData,
            overriddenPaymentMethodInformation = expectedOverriddenPaymentMethodInformation,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when both checkout and drop-in configurations are set then params should match checkout configuration`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "EUR",
                value = 49_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            dropIn {
                setShopperLocale(Locale.US)
                setAmount(Amount("USD", 12))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val params = dropInParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null)

        val expected = getDropInParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            amount = Amount(
                currency = "EUR",
                value = 49_00L,
            ),
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions then configuration`(
        configurationValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount?,
    ) {
        val testConfiguration = createCheckoutConfiguration(configurationValue)

        val sessionParams = SessionParams(
            enableStoreDetails = null,
            installmentConfiguration = null,
            amount = sessionsValue,
            returnUrl = null,
            shopperLocale = null,
        )

        val params = dropInParamsMapper.mapToParams(
            checkoutConfiguration = testConfiguration,
            deviceLocale = DEVICE_LOCALE,
            sessionParams = sessionParams,
        )

        val expected = getDropInParams(amount = expectedValue)

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
        val configuration = createCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = SessionParams(
            enableStoreDetails = null,
            installmentConfiguration = null,
            amount = null,
            returnUrl = "",
            shopperLocale = sessionsValue,
        )

        val params = dropInParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            sessionParams = sessionParams,
        )

        val expected = getDropInParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configurationBlock: DropInConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        shopperLocale = shopperLocale,
        amount = amount,
    ) {
        dropIn(configurationBlock)
    }

    @Suppress("LongParameterList")
    private fun getDropInParams(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        shopperLocale: Locale = DEVICE_LOCALE,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount: Amount? = null,
        showPreselectedStoredPaymentMethod: Boolean = true,
        skipListWhenSinglePaymentMethod: Boolean = false,
        isRemovingStoredPaymentMethodsEnabled: Boolean = false,
        additionalDataForDropInService: Bundle? = null,
        overriddenPaymentMethodInformation: Map<String, DropInPaymentMethodInformation> = emptyMap(),
    ): DropInParams {
        return DropInParams(
            environment = environment,
            clientKey = clientKey,
            shopperLocale = shopperLocale,
            analyticsParams = analyticsParams,
            amount = amount,
            showPreselectedStoredPaymentMethod = showPreselectedStoredPaymentMethod,
            skipListWhenSinglePaymentMethod = skipListWhenSinglePaymentMethod,
            isRemovingStoredPaymentMethodsEnabled = isRemovingStoredPaymentMethodsEnabled,
            additionalDataForDropInService = additionalDataForDropInService,
            overriddenPaymentMethodInformation = overriddenPaymentMethodInformation,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), null, Amount("EUR", 100)),
            arguments(null, Amount("CAD", 300), Amount("CAD", 300)),
            arguments(null, null, null),
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
