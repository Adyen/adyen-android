/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.boleto.boleto
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

internal class BoletoComponentParamsMapperTest {

    private val boletoComponentParamsMapper = BoletoComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom boleto configuration fields are null, them all fields should match`() {
        val params = mapParams()
        val expected = getBoletoComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom fields are set then all fields should match`() {
        val configuration = createCheckoutConfiguration {
            setEmailVisibility(true)
        }

        val params = mapParams(configuration)
        val expectedAddressParams = AddressParams.FullAddress(
            defaultCountryCode = BRAZIL_COUNTRY_CODE,
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST_1,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        )
        val expected = getBoletoComponentParams(
            addressParams = expectedAddressParams,
            isSendEmailVisible = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override Boleto configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "CAD",
                value = 123_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            boleto {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 20L), null)
        val params = mapParams(
            configuration = configuration,
            dropInOverrideParams = dropInOverrideParams,
        )

        val expected = getBoletoComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.INITIAL, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 20L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in boleto configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            boleto {
                setSubmitButtonVisible(false)
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 20L), null)
        val params = mapParams(
            configuration = configuration,
            dropInOverrideParams = dropInOverrideParams,
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @Test
    fun `when setSubmitButtonVisible is not set in boleto component configuration and checkout configuration does set it, then it should follow that`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isSubmitButtonVisible = false,
        ) {
            boleto { }
        }

        val params = mapParams(configuration = configuration)

        assertEquals(false, params.isSubmitButtonVisible)
    }

    @Test
    fun `when send email is set, them params should match`() {
        val configuration = createCheckoutConfiguration {
            setEmailVisibility(true)
        }

        val params = mapParams(configuration)
        val expected = getBoletoComponentParams(
            isSendEmailVisible = true,
        )

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

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )

        val params = mapParams(
            configuration = configuration,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
        )

        val expected = getBoletoComponentParams(
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

        val params = boletoComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getBoletoComponentParams(
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

        val params = boletoComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
        )

        val expected = getBoletoComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: BoletoConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        boleto(configuration)
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
    private fun getBoletoComponentParams(
        isSubmitButtonVisible: Boolean = true,
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        addressParams: AddressParams = AddressParams.FullAddress(
            defaultCountryCode = BRAZIL_COUNTRY_CODE,
            supportedCountryCodes = SUPPORTED_COUNTRY_LIST_1,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        ),
        isSendEmailVisible: Boolean = false
    ) = BoletoComponentParams(
        isSubmitButtonVisible = isSubmitButtonVisible,
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        addressParams = addressParams,
        isEmailVisible = isSendEmailVisible,
    )

    private fun mapParams(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        locale: Locale = DEVICE_LOCALE,
        dropInOverrideParams: DropInOverrideParams? = null,
        componentSessionParams: SessionParams? = null,
    ): BoletoComponentParams {
        return boletoComponentParamsMapper.mapToParams(
            configuration,
            locale,
            dropInOverrideParams,
            componentSessionParams,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val BRAZIL_COUNTRY_CODE = "BR"
        private val SUPPORTED_COUNTRY_LIST_1 = listOf(BRAZIL_COUNTRY_CODE)
        private val DEVICE_LOCALE = Locale("nl", "NL")

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
