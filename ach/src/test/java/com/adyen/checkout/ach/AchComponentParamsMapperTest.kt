/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 27/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.ui.AddressParams
import com.adyen.checkout.core.api.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AchComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom ach configuration fields are null then all fields should match`() {
        val achConfiguration = getAchConfigurationBuilder().build()

        val params = AchComponentParamsMapper(null).mapToParams(achConfiguration)

        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom ach configuration fields are set then all fields should match`() {
        val addressConfiguration = AddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()
        val params = AchComponentParamsMapper(null).mapToParams(achConfiguration)
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

        val params = AchComponentParamsMapper(overrideParams).mapToParams(achConfiguration)

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
        val addressConfiguration = AddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()

        val params = AchComponentParamsMapper(null).mapToParams(achConfiguration)
        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as None, addressParams should return None`() {
        val addressConfiguration = AddressConfiguration.None
        val achConfiguration = getAchConfigurationBuilder().apply {
            setAddressConfiguration(addressConfiguration)
        }.build()

        val params = AchComponentParamsMapper(null).mapToParams(achConfiguration)
        val expected = getAchComponentParams(addressParams = AddressParams.None)

        assertEquals(expected, params)
    }

    private fun getAchConfigurationBuilder() = AchConfiguration.Builder(
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
        )
    ) = AchComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        isSubmitButtonVisible = isSubmitButtonVisible,
        addressParams = addressParams
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val SUPPORTED_COUNTRY_LIST = listOf("US", "PR")
    }
}
