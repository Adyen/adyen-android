/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.boleto.boletoConfiguration
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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class BoletoComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom boleto configuration fields are null, them all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = getBoletoComponentParamsMapper().mapToParams(configuration, null)
        val expected = getBoletoComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom fields are set then all fields should match`() {
        val configuration = createCheckoutConfiguration {
            setEmailVisibility(true)
        }

        val params = getBoletoComponentParamsMapper().mapToParams(configuration, null)
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
    fun `when parent configuration is set then parent configuration should override Boleto configuration fields`() {
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
            boletoConfiguration {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val params = getBoletoComponentParamsMapper(isCreatedByDropIn = true).mapToParams(
            configuration,
            null,
        )

        val expected = getBoletoComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123_00L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when send email is set, them params should match`() {
        val configuration = createCheckoutConfiguration {
            setEmailVisibility(true)
        }

        val params = getBoletoComponentParamsMapper().mapToParams(configuration, null)
        val expected = getBoletoComponentParams(
            isSendEmailVisible = true,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val params = getBoletoComponentParamsMapper().mapToParams(
            configuration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
            ),
        )

        val expected = getBoletoComponentParams(
            amount = expectedValue,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: BoletoConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        boletoConfiguration(configuration)
    }

    @Suppress("LongParameterList")
    private fun getBoletoComponentParams(
        isSubmitButtonVisible: Boolean = true,
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
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
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        addressParams = addressParams,
        isEmailVisible = isSendEmailVisible,
    )

    private fun getBoletoComponentParamsMapper(
        isCreatedByDropIn: Boolean = false,
        overrideSessionParams: SessionParams? = null,
    ) = BoletoComponentParamsMapper(isCreatedByDropIn, overrideSessionParams)

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val BRAZIL_COUNTRY_CODE = "BR"
        private val SUPPORTED_COUNTRY_LIST_1 = listOf(BRAZIL_COUNTRY_CODE)

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            Arguments.arguments(Amount("EUR", 100), Amount("CAD", 300), Amount("CAD", 300)),
            Arguments.arguments(Amount("EUR", 100), null, Amount("EUR", 100)),
        )
    }
}
