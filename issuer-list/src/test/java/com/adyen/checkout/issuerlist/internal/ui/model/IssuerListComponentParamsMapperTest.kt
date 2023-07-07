/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
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
        val issuerListConfiguration = getTestIssuerListConfigurationBuilder().build()

        val params = IssuerListComponentParamsMapper(null, null).mapToParams(issuerListConfiguration, null)

        val expected = getIssuerListComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom issuer list configuration fields are set then all fields should match`() {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setHideIssuerLogos(true)
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .setSubmitButtonVisible(false)
            .build()

        val params = IssuerListComponentParamsMapper(null, null).mapToParams(issuerListConfiguration, null)

        val expected = IssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = false
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override issuer list configuration fields`() {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setHideIssuerLogos(true)
            .setViewType(IssuerListViewType.SPINNER_VIEW)
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
                currency = "XCD",
                value = 4_00L
            )
        )

        val params = IssuerListComponentParamsMapper(overrideParams, null).mapToParams(issuerListConfiguration, null)

        val expected = IssuerListComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
            amount = Amount(
                currency = "XCD",
                value = 4_00L
            ),
            isSubmitButtonVisible = true
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
        val issuerListConfiguration = getTestIssuerListConfigurationBuilder()
            .setAmount(configurationValue)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = dropInValue?.let { getIssuerListComponentParams().copy(amount = it) }

        val params = IssuerListComponentParamsMapper(overrideParams, null).mapToParams(
            issuerListConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentOptions = null,
                amount = sessionsValue,
                returnUrl = "",
            )
        )

        val expected = getIssuerListComponentParams().copy(amount = expectedValue)

        assertEquals(expected, params)
    }

    private fun getTestIssuerListConfigurationBuilder(): TestIssuerListConfiguration.Builder {
        return TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
    }

    private fun getIssuerListComponentParams(): IssuerListComponentParams {
        return IssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.RECYCLER_VIEW,
            hideIssuerLogos = false,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = true
        )
    }

    companion object {
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
