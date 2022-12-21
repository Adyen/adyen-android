/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class BcmcComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom bcmc configuration fields are null then all fields should match`() {
        val bcmcConfiguration = BcmcConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .build()

        val params = BcmcComponentParamsMapper(null).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            isHolderNameRequired = false,
            shopperReference = null,
            isStorePaymentFieldVisible = false,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = true
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom bcmc configuration fields are set then all fields should match`() {
        val shopperReference = "SHOPPER_REFERENCE_1"

        val bcmcConfiguration = BcmcConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setShopperReference(shopperReference)
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setSubmitButtonVisible(false)
            .build()

        val params = BcmcComponentParamsMapper(null).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            isHolderNameRequired = true,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = true,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = false
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override bcmc configuration fields`() {
        val bcmcConfiguration = BcmcConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
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
                currency = "USD",
                value = 25_00L
            )
        )

        val params = BcmcComponentParamsMapper(overrideParams).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            isHolderNameRequired = false,
            shopperReference = null,
            isStorePaymentFieldVisible = false,
            amount = Amount(
                currency = "USD",
                value = 25_00L
            ),
            isSubmitButtonVisible = true
        )

        assertEquals(expected, params)
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
