/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class BcmcComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom bcmc configuration fields are null then all fields should match`() {
        val bcmcConfiguration = getBcmcConfigurationBuilder()
            .build()

        val params = BcmcComponentParamsMapper(null, null).mapToParams(bcmcConfiguration, null)

        val expected = getBcmcComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom bcmc configuration fields are set then all fields should match`() {
        val shopperReference = "SHOPPER_REFERENCE_1"

        val bcmcConfiguration = getBcmcConfigurationBuilder()
            .setShopperReference(shopperReference)
            .setHolderNameRequired(true)
            .setShowStorePaymentField(true)
            .setSubmitButtonVisible(false)
            .build()

        val params = BcmcComponentParamsMapper(null, null).mapToParams(bcmcConfiguration, null)

        val expected = getBcmcComponentParams(
            isHolderNameRequired = true,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = true,
            isSubmitButtonVisible = false
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override bcmc configuration fields`() {
        val bcmcConfiguration = getBcmcConfigurationBuilder()
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

        val params = BcmcComponentParamsMapper(overrideParams, null).mapToParams(bcmcConfiguration, null)

        val expected = getBcmcComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "USD",
                value = 25_00L
            ),
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("sessionSetupConfigurationSource")
    @Suppress("MaxLineLength")
    fun `is store payment field visible should match enable store details from session setup response whatever the value of show store payment field inside Bcmc configurations`(
        showStorePaymentField: Boolean,
        enableStoreDetails: Boolean,
        isStorePaymentFieldVisible: Boolean
    ) {
        val bcmcConfiguration = getBcmcConfigurationBuilder()
            .setShowStorePaymentField(showStorePaymentField)
            .build()

        val params = BcmcComponentParamsMapper(null, null).mapToParams(
            bcmcConfiguration = bcmcConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = enableStoreDetails,
                installmentOptions = null,
                amount = null
            )
        )

        val expected = getBcmcComponentParams(isStorePaymentFieldVisible = isStorePaymentFieldVisible)

        assertEquals(expected, params)
    }

    private fun getBcmcConfigurationBuilder() = BcmcConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1
    )

    private fun getBcmcComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        isAnalyticsEnabled: Boolean = true,
        isCreatedByDropIn: Boolean = false,
        amount: Amount = Amount.EMPTY,
        isSubmitButtonVisible: Boolean = true,
        isHolderNameRequired: Boolean = false,
        shopperReference: String? = null,
        isStorePaymentFieldVisible: Boolean = false,
    ) = BcmcComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        isSubmitButtonVisible = isSubmitButtonVisible,
        isHolderNameRequired = isHolderNameRequired,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"

        @JvmStatic
        fun sessionSetupConfigurationSource() = listOf(
            // showStorePaymentField, enableStoreDetails, isStorePaymentFieldVisible
            arguments(false, false, false),
            arguments(false, true, true),
            arguments(true, false, false),
            arguments(true, true, true),
        )
    }
}
