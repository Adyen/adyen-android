/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class BacsDirectDebitComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom bacs configuration fields are null then all fields should match`() {
        val bacsDirectDebitConfiguration = BacsDirectDebitConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .build()

        val params = BacsDirectDebitComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(bacsDirectDebitConfiguration)

        val expected = BacsDirectDebitComponentParams(
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
    fun `when parent configuration is null and custom bacs configuration fields are set then all fields should match`() {
        val amount = Amount(currency = "EUR", value = 1337)
        val bacsDirectDebitConfiguration = BacsDirectDebitConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setAmount(amount)
            .build()

        val params = BacsDirectDebitComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(bacsDirectDebitConfiguration)

        val expected = BacsDirectDebitComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = amount
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override bacs configuration fields`() {
        val amount = Amount(currency = "EUR", value = 1337)
        val bacsDirectDebitConfiguration = BacsDirectDebitConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setAmount(amount)
            .build()

        // this is in practice DropInConfiguration, but we don't have access to it in this module and any Configuration
        // class can work
        val parentConfiguration = BacsDirectDebitConfiguration.Builder(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        )
            .setAnalyticsEnabled(false)
            .build()

        val params = BacsDirectDebitComponentParamsMapper(
            parentConfiguration = parentConfiguration,
            isCreatedByDropIn = true
        ).mapToParams(bacsDirectDebitConfiguration)

        val expected = BacsDirectDebitComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = amount
        )

        assertEquals(expected, params)
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
