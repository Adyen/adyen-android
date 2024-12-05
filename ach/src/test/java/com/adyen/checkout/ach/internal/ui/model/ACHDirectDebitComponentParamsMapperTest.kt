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
import com.adyen.checkout.ach.achDirectDebit
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
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

    private val achDirectDebitComponentParamsMapper = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom ach configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom ach configuration fields are set then all fields should match`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }
        val params = achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)
        val expected = getAchComponentParams()
        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override ach configuration fields`() {
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
            achDirectDebit {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params = achDirectDebitComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
        )

        val expected = getAchComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.INITIAL, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in ach configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            achDirectDebit {
                setSubmitButtonVisible(false)
            }
        }
        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params = achDirectDebitComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @Test
    fun `when a address is selected as FullAddress, addressParams should return FullAddress`() {
        val addressConfiguration =
            ACHDirectDebitAddressConfiguration.FullAddress(supportedCountryCodes = SUPPORTED_COUNTRY_LIST)
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }

        val params = achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)
        val expected = getAchComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when a address is selected as None, addressParams should return None`() {
        val addressConfiguration = ACHDirectDebitAddressConfiguration.None
        val configuration = createCheckoutConfiguration {
            setAddressConfiguration(addressConfiguration)
        }

        val params =
            achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)
        val expected = getAchComponentParams(addressParams = AddressParams.None)

        assertEquals(expected, params)
    }

    @Test
    fun `when the address configuration is null, default address configuration should be FullAddress with default supported countries`() {
        val configuration = createCheckoutConfiguration()

        val params =
            achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

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

        val params =
            achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = false)

        assertEquals(expected, params)
    }

    @Test
    fun `when the isStorePaymentFieldVisible  in configuration is true, isStorePaymentFieldVisible in component params should be true`() {
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(true)
        }

        val params =
            achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

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

        val sessionParams = createSessionParams(
            enableStoreDetails = sessionsValue,
        )

        val params = achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, sessionParams)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = expectedValue)

        assertEquals(expected, params)
    }

    @Test
    fun `when isStorePaymentFieldVisible is not set, isStorePaymentFieldVisible should be true`() {
        val configuration = createCheckoutConfiguration()

        val params = achDirectDebitComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getAchComponentParams(isStorePaymentFieldVisible = true)

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions then drop in then component configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val params = achDirectDebitComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
        )

        val expected = getAchComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `shopper locale should match value set in configuration then sessions then device locale`(
        configurationValue: Locale?,
        sessionsValue: Locale?,
        deviceLocaleValue: Locale,
        expectedValue: Locale,
    ) {
        val configuration = createCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = createSessionParams(
            shopperLocale = sessionsValue,
        )

        val params = achDirectDebitComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getAchComponentParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `environment and client key should match value set in sessions`() {
        val configuration = createCheckoutConfiguration()

        val sessionParams = createSessionParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        val params = achDirectDebitComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getAchComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: ACHDirectDebitConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        achDirectDebit(configuration)
    }

    @Suppress("LongParameterList")
    private fun createSessionParams(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        enableStoreDetails: Boolean? = null,
        installmentConfiguration: SessionInstallmentConfiguration? = null,
        showRemovePaymentMethodButton: Boolean? = null,
        amount: Amount? = null,
        returnUrl: String? = "",
        shopperLocale: Locale? = null,
    ) = SessionParams(
        environment = environment,
        clientKey = clientKey,
        enableStoreDetails = enableStoreDetails,
        installmentConfiguration = installmentConfiguration,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
        amount = amount,
        returnUrl = returnUrl,
        shopperLocale = shopperLocale,
    )

    @Suppress("LongParameterList")
    private fun getAchComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
        addressParams: AddressParams = AddressParams.FullAddress(
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        ),
        isStorePaymentFieldVisible: Boolean = true
    ) = ACHDirectDebitComponentParams(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        isSubmitButtonVisible = isSubmitButtonVisible,
        addressParams = addressParams,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val SUPPORTED_COUNTRY_LIST = listOf("US", "PR")
        private val DEVICE_LOCALE = Locale("nl", "NL")

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

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            // configurationValue, sessionsValue, deviceLocaleValue, expectedValue
            arguments(null, null, Locale.US, Locale.US),
            arguments(Locale.GERMAN, null, Locale.US, Locale.GERMAN),
            arguments(null, Locale.CHINESE, Locale.US, Locale.CHINESE),
            arguments(Locale.GERMAN, Locale.CHINESE, Locale.US, Locale.GERMAN),
        )
    }
}
