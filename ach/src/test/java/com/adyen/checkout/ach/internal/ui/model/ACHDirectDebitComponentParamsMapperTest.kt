/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.ach.ACHDirectDebitAddressConfiguration
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class ACHDirectDebitComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom ach configuration fields are null then all fields should match`() {
        val achConfiguration = getAchConfigurationBuilder().build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom ach configuration fields are set then all fields should match`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val achConfiguration = getAchConfigurationBuilder()
            .setAddressConfiguration(addressConfiguration)
            .build()
        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)
        val expected = getAchComponentParams()
        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override ach configuration fields`() {
        val achConfiguration = getAchConfigurationBuilder().build()

        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L
            )
        )

        val params = ACHDirectDebitComponentParamsMapper(overrideParams, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L
            )
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as FullAddress, addressParams should return FullAddress`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val achConfiguration = getAchConfigurationBuilder()
            .setAddressConfiguration(addressConfiguration)
            .build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)
        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as None, addressParams should return None`() {
        val addressConfiguration = ACHDirectDebitAddressConfiguration.None
        val achConfiguration = getAchConfigurationBuilder()
            .setAddressConfiguration(addressConfiguration)
            .build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)
        val expected = getAchComponentParams(addressParams = AddressParams.None)

        assertEquals(expected, params)
    }

    @Test
    fun `when the address configuration is null, default address configuration should be FullAddress with default supported countries`() {
        val achConfiguration = getAchConfigurationBuilder().build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expectedAddressParams: AddressParams = AddressParams.FullAddress(
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST,
            addressFieldPolicy = AddressFieldPolicyParams.Required
        )

        assertEquals(expectedAddressParams, params.addressParams)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is false, isStorePaymentFieldVisible in component params should be false`() {
        val achConfiguration = getAchConfigurationBuilder()
            .setShowStorePaymentField(false)
            .build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = false)

        assertEquals(expected, params)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is true, isStorePaymentFieldVisible in component params should be true`() {
        val achConfiguration = getAchConfigurationBuilder()
            .setShowStorePaymentField(true)
            .build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("enableStoreDetailsSource")
    @Suppress("MaxLineLength")
    fun `isStorePaymentFieldVisible should match value set in sessions if it exists, otherwise should match configuration`(
        configurationValue: Boolean,
        sessionsValue: Boolean?,
        expectedValue: Boolean
    ) {
        val achConfiguration = getAchConfigurationBuilder()
            .setShowStorePaymentField(configurationValue)
            .build()

        val sessionParams = SessionParams(
            enableStoreDetails = sessionsValue,
            installmentOptions = null,
            amount = null
        )

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(
            configuration = achConfiguration,
            sessionParams = sessionParams
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = expectedValue)

        assertEquals(expected, params)
    }

    @Test
    fun `when isStorePaymentFieldVisible is not set, isStorePaymentFieldVisible should be true`() {
        val achConfiguration = getAchConfigurationBuilder().build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(
            configuration = achConfiguration,
            sessionParams = null
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

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
        val achConfiguration = getAchConfigurationBuilder()
            .setAmount(configurationValue)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = dropInValue?.let { getAchComponentParams(amount = it) }

        val params = ACHDirectDebitComponentParamsMapper(overrideParams, null).mapToParams(
            achConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentOptions = null,
                amount = sessionsValue
            )
        )

        val expected = getAchComponentParams(
            amount = expectedValue
        )

        assertEquals(expected, params)
    }

    private fun getAchConfigurationBuilder() = ACHDirectDebitConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
    )

    @Suppress("LongParameterList")
    private fun getAchComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        isAnalyticsEnabled: Boolean = true,
        isCreatedByDropIn: Boolean = false,
        amount: Amount = Amount.EMPTY,
        isSubmitButtonVisible: Boolean = true,
        addressParams: AddressParams = AddressParams.FullAddress(
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST,
            addressFieldPolicy = AddressFieldPolicyParams.Required
        ),
        isStorePaymentFieldVisible: Boolean = true
    ) = ACHDirectDebitComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        isSubmitButtonVisible = isSubmitButtonVisible,
        addressParams = addressParams,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val SUPPORTED_COUNTRY_LIST = listOf("US", "PR")

        @JvmStatic
        fun enableStoreDetailsSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(false, false, false),
            arguments(false, true, true),
            arguments(true, false, false),
            arguments(true, true, true),
            arguments(false, null, false),
            arguments(true, null, true),
        )

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )
    }
}
