/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.TestConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class GenericComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null then params should match the component configuration`() {
        val componentConfiguration = getTestConfigurationBuilder().build()

        val params = GenericComponentParamsMapper(null, null).mapToParams(componentConfiguration, null)

        val expected = getGenericComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override component configuration fields`() {
        val componentConfiguration = TestConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        ).build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 49_00L
            )
        )

        val params = GenericComponentParamsMapper(overrideParams, null).mapToParams(
            componentConfiguration,
            null
        )

        val expected = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 49_00L
            )
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
        val testConfiguration = getTestConfigurationBuilder()
            .setAmount(configurationValue)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = dropInValue?.let { getGenericComponentParams().copy(amount = it) }

        val params = GenericComponentParamsMapper(overrideParams, null).mapToParams(
            testConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentOptions = null,
                amount = sessionsValue
            )
        )

        val expected = getGenericComponentParams().copy(amount = expectedValue)

        assertEquals(expected, params)
    }

    private fun getTestConfigurationBuilder(): TestConfiguration.Builder {
        return TestConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
    }

    private fun getGenericComponentParams(): GenericComponentParams {
        return GenericComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY
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
