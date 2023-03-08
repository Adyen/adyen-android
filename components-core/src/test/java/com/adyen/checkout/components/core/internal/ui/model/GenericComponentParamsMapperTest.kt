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
import java.util.Locale

internal class GenericComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null then params should match the component configuration`() {
        val componentConfiguration = TestConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        ).build()

        val params = GenericComponentParamsMapper(null, null).mapToParams(componentConfiguration, null)

        val expected = GenericComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY
        )

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

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
