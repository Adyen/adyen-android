/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc

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

        val params = BcmcComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isCreatedByDropIn = false,
            isHolderNameRequired = false,
            shopperReference = null,
            isStorePaymentFieldVisible = false,
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
            .build()

        val params = BcmcComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isCreatedByDropIn = false,
            isHolderNameRequired = true,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = true,
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

        // this is in practice DropInConfiguration, but we don't have access to it in this module and any Configuration
        // class can work
        val parentConfiguration = BcmcConfiguration.Builder(
            Locale.GERMAN,
            Environment.EUROPE,
            TEST_CLIENT_KEY_2
        ).build()

        val params =
            BcmcComponentParamsMapper(
                parentConfiguration = parentConfiguration,
                isCreatedByDropIn = true
            ).mapToParams(bcmcConfiguration)

        val expected = BcmcComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isCreatedByDropIn = true,
            isHolderNameRequired = false,
            shopperReference = null,
            isStorePaymentFieldVisible = false,
        )

        assertEquals(expected, params)
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
