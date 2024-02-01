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

    @Test
    fun `when parent configuration is null and custom issuer list configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = IssuerListComponentParamsMapper(null, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = null,
        )

        val expected = getIssuerListComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom issuer list configuration fields are set then all fields should match`() {
        val configuration = createCheckoutConfiguration {
            setHideIssuerLogos(true)
            setViewType(IssuerListViewType.SPINNER_VIEW)
            setSubmitButtonVisible(false)
        }

        val params = IssuerListComponentParamsMapper(null, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = null,
        )

        val expected = IssuerListComponentParams(
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
    fun `when parent configuration is set then parent configuration fields should override issuer list configuration fields`() {
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

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L))
        val params = IssuerListComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = null,
        )

        val expected = IssuerListComponentParams(
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
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it) }

        val params = IssuerListComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
            ),
        )

        val expected = getIssuerListComponentParams().copy(
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

    private fun getIssuerListComponentParams(): IssuerListComponentParams {
        return IssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.RECYCLER_VIEW,
            hideIssuerLogos = false,
            amount = null,
            isSubmitButtonVisible = true,
        )
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )
    }
}
