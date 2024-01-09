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
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
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
        val configuration = createCheckoutConfiguration()

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)

        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom ach configuration fields are set then all fields should match`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }
        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)
        val expected = getAchComponentParams()
        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override ach configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            ACHDirectDebitConfiguration {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val params = ACHDirectDebitComponentParamsMapper(true, null).mapToParams(configuration, null)

        val expected = getAchComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as FullAddress, addressParams should return FullAddress`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)
        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as None, addressParams should return None`() {
        val addressConfiguration = ACHDirectDebitAddressConfiguration.None
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)
        val expected = getAchComponentParams(addressParams = AddressParams.None)

        assertEquals(expected, params)
    }

    @Test
    fun `when the address configuration is null, default address configuration should be FullAddress with default supported countries`() {
        val configuration = createCheckoutConfiguration()

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)

        val expectedAddressParams: AddressParams = AddressParams.FullAddress(
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        )

        assertEquals(expectedAddressParams, params.addressParams)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is false, isStorePaymentFieldVisible in component params should be false`() {
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(false)
        }

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = false)

        assertEquals(expected, params)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is true, isStorePaymentFieldVisible in component params should be true`() {
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(true)
        }

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(configuration, null)

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
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(configurationValue)
        }

        val sessionParams = SessionParams(
            enableStoreDetails = sessionsValue,
            installmentConfiguration = null,
            amount = null,
            returnUrl = "",
        )

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            sessionParams = sessionParams,
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = expectedValue)

        assertEquals(expected, params)
    }

    @Test
    fun `when isStorePaymentFieldVisible is not set, isStorePaymentFieldVisible should be true`() {
        val configuration = createCheckoutConfiguration()

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            sessionParams = null,
        )

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then configuration`(
        configurationValue: Amount,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val params = ACHDirectDebitComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
            ),
        )

        val expected = getAchComponentParams(
            amount = expectedValue,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: ACHDirectDebitConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        ACHDirectDebitConfiguration(configuration)
    }

    @Suppress("LongParameterList")
    private fun getAchComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
        addressParams: AddressParams = AddressParams.FullAddress(
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        ),
        isStorePaymentFieldVisible: Boolean = true
    ) = ACHDirectDebitComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        isSubmitButtonVisible = isSubmitButtonVisible,
        addressParams = addressParams,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
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
            // configurationValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), null, Amount("EUR", 100)),
        )
    }
}
