/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.TestConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class GenericComponentParamsMapperTest {

    private val genericComponentParamsMapper = GenericComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration()

        val params = genericComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getGenericComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override component configuration fields`() {
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
            val testConfiguration = TestConfiguration.Builder(
                shopperLocale = Locale.US,
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY_1,
            )
                .setAmount(Amount("USD", 1L))
                .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
                .build()
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = genericComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, dropInOverrideParams, null)

        val expected = getGenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
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
        val testConfiguration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val sessionParams = SessionParams(
            enableStoreDetails = null,
            installmentConfiguration = null,
            amount = sessionsValue,
            returnUrl = "",
        )

        val params = genericComponentParamsMapper.mapToParams(
            testConfiguration,
            DEVICE_LOCALE,
            dropInOverrideParams,
            sessionParams,
        )

        val expected = getGenericComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        val testConfiguration = TestConfiguration.Builder(shopperLocale, environment, clientKey)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
    }

    @Suppress("LongParameterList")
    private fun getGenericComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
    ): GenericComponentParams {
        return GenericComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsParams = analyticsParams,
                isCreatedByDropIn = isCreatedByDropIn,
                amount = amount,
            ),
        )
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )
    }
}
