/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.utils.TestIssuerListConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class IssuerListComponentParamsMapperTest {

    private val issuerListComponentParamsMapper = IssuerListComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom issuer list configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = issuerListComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            hideIssuerLogosDefaultValue = false,
        )

        val expected = getIssuerListComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom issuer list configuration fields are set then all fields should match`() {
        val configuration = createCheckoutConfiguration {
            setHideIssuerLogos(true)
            setViewType(IssuerListViewType.SPINNER_VIEW)
            setSubmitButtonVisible(false)
        }

        val params = issuerListComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            hideIssuerLogosDefaultValue = false,
        )

        val expected = getIssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
            amount = null,
            isSubmitButtonVisible = false,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override issuer list configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "XCD",
                value = 4_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            val issuerListConfiguration = TestIssuerListConfiguration.Builder(shopperLocale, environment, clientKey)
                .setHideIssuerLogos(true)
                .setViewType(IssuerListViewType.SPINNER_VIEW)
                .setAmount(Amount("USD", 1L))
                .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
                .build()
            addConfiguration(TEST_CONFIGURATION_KEY, issuerListConfiguration)
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = issuerListComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            hideIssuerLogosDefaultValue = false,
        )

        val expected = getIssuerListComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
            isSubmitButtonVisible = true,
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
        val sessionParams = SessionParams(
            enableStoreDetails = null,
            installmentConfiguration = null,
            amount = sessionsValue,
            returnUrl = "",
        )
        val params = issuerListComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            hideIssuerLogosDefaultValue = false,
        )

        val expected = getIssuerListComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: TestIssuerListConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(shopperLocale, environment, clientKey)
            .apply(configuration)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, issuerListConfiguration)
    }

    @Suppress("LongParameterList")
    private fun getIssuerListComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
        viewType: IssuerListViewType = IssuerListViewType.RECYCLER_VIEW,
        hideIssuerLogos: Boolean = false,
    ): IssuerListComponentParams {
        return IssuerListComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsParams = analyticsParams,
                isCreatedByDropIn = isCreatedByDropIn,
                amount = amount,
            ),
            isSubmitButtonVisible = isSubmitButtonVisible,
            viewType = viewType,
            hideIssuerLogos = hideIssuerLogos,
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
