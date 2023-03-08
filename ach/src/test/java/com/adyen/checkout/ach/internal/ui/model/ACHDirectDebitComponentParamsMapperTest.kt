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
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()
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
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)
        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as None, addressParams should return None`() {
        val addressConfiguration = ACHDirectDebitAddressConfiguration.None
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()

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
        val achConfiguration = getAchConfigurationBuilder().apply {
            setShowStorePaymentField(false)
        }.build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = false)

        assertEquals(expected, params)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is true, isStorePaymentFieldVisible in component params should be true`() {
        val achConfiguration = getAchConfigurationBuilder().apply {
            setShowStorePaymentField(true)
        }.build()

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(achConfiguration, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

        assertEquals(expected, params)
    }

    @Test
    fun `when the enableStoreDetails in SessionSetupConfiguration is true, isStorePaymentFieldVisible in component params should be true`() {
        val achConfiguration = getAchConfigurationBuilder().apply {
            setShowStorePaymentField(false)
        }.build()

        val sessionParams = SessionParams(enableStoreDetails = true, installmentOptions = null)

        val params = ACHDirectDebitComponentParamsMapper(null, sessionParams).mapToParams(
            configuration = achConfiguration,
            sessionParams = null
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

        assertEquals(expected, params)
    }

    @Test
    fun `when the enableStoreDetails in SessionSetupConfiguration is false, isStorePaymentFieldVisible in component params should be false`() {
        val achConfiguration = getAchConfigurationBuilder().apply {
            setShowStorePaymentField(true)
        }.build()

        val sessionParams = SessionParams(enableStoreDetails = false, installmentOptions = null)

        val params = ACHDirectDebitComponentParamsMapper(null, null).mapToParams(
            configuration = achConfiguration,
            sessionParams = sessionParams
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = false)

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

    private fun getAchConfigurationBuilder() = ACHDirectDebitConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
    )

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
    }
}
