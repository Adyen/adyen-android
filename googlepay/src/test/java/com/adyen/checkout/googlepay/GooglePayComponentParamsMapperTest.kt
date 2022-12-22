/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.model.paymentmethods.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.googlepay.model.BillingAddressParameters
import com.adyen.checkout.googlepay.model.MerchantInfo
import com.adyen.checkout.googlepay.model.ShippingAddressParameters
import com.adyen.checkout.googlepay.util.AllowedAuthMethods
import com.adyen.checkout.googlepay.util.AllowedCardNetworks
import com.google.android.gms.wallet.WalletConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

internal class GooglePayComponentParamsMapperTest {

    @BeforeEach
    fun beforeEach() {
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when parent configuration is null and custom google pay configuration fields are null then all fields should match`() {
        val googlePayConfiguration = getGooglePayConfigurationBuilder().build()

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom google pay configuration fields are set then all fields should match`() {
        val amount = Amount("EUR", 1337)
        val merchantInfo = MerchantInfo("MERCHANT_NAME", "MERCHANT_ID")
        val allowedAuthMethods = listOf("AUTH1", "AUTH2", "AUTH3")
        val allowedCardNetworks = listOf("CARD1", "CARD2")
        val shippingAddressParameters = ShippingAddressParameters(listOf("ZZ", "AA"), true)
        val billingAddressParameters = BillingAddressParameters("FORMAT", true)

        val googlePayConfiguration = GooglePayConfiguration.Builder(
            shopperLocale = Locale.FRANCE, environment = Environment.APSE, clientKey = TEST_CLIENT_KEY_2
        ).setAmount(amount).setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .setMerchantAccount("MERCHANT_ACCOUNT").setAllowPrepaidCards(true).setCountryCode("ZZ")
            .setMerchantInfo(merchantInfo).setAllowedAuthMethods(allowedAuthMethods)
            .setAllowedCardNetworks(allowedCardNetworks).setBillingAddressParameters(billingAddressParameters)
            .setBillingAddressRequired(true).setEmailRequired(true).setExistingPaymentMethodRequired(true)
            .setShippingAddressParameters(shippingAddressParameters).setShippingAddressRequired(true)
            .setTotalPriceStatus("STATUS").build()

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            gatewayMerchantId = "MERCHANT_ACCOUNT",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            amount = amount,
            totalPriceStatus = "STATUS",
            countryCode = "ZZ",
            merchantInfo = merchantInfo,
            allowedAuthMethods = allowedAuthMethods,
            allowedCardNetworks = allowedCardNetworks,
            isAllowPrepaidCards = true,
            isEmailRequired = true,
            isExistingPaymentMethodRequired = true,
            isShippingAddressRequired = true,
            shippingAddressParameters = shippingAddressParameters,
            isBillingAddressRequired = true,
            billingAddressParameters = billingAddressParameters,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override google pay configuration fields`() {
        val googlePayConfiguration = getGooglePayConfigurationBuilder().build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "XCD",
                value = 4_00L
            )
        )

        val params = GooglePayComponentParamsMapper(overrideParams).mapToParams(
            googlePayConfiguration, PaymentMethod()
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "XCD",
                value = 4_00L
            )
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is set in googlePayConfiguration then it takes priority over gatewayMerchantId in the paymentMethod configuration`() {
        val googlePayConfiguration = GooglePayConfiguration.Builder(
            shopperLocale = Locale.US, environment = Environment.TEST, clientKey = TEST_CLIENT_KEY_1
        ).setMerchantAccount("GATEWAY_MERCHANT_ID_1").build()

        val paymentMethod = PaymentMethod(
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2"
            )
        )

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, paymentMethod)

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_1"
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is not set in googlePayConfiguration then gatewayMerchantId in the paymentMethod configuration is used`() {
        val googlePayConfiguration = GooglePayConfiguration.Builder(
            shopperLocale = Locale.US, environment = Environment.TEST, clientKey = TEST_CLIENT_KEY_1
        ).build()

        val paymentMethod = PaymentMethod(
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2"
            )
        )

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, paymentMethod)

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_2"
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when neither merchantAccount in googlePayConfiguration nor gatewayMerchantId in the paymentMethod configuration is set then exception is thrown`() {
        val googlePayConfiguration = GooglePayConfiguration.Builder(
            shopperLocale = Locale.US, environment = Environment.TEST, clientKey = TEST_CLIENT_KEY_1
        ).build()

        assertThrows<ComponentException> {
            GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())
        }
    }

    @Test
    fun `when allowedCardNetworks is not set in googlePayConfiguration then brands in the paymentMethod is used`() {
        val googlePayConfiguration = getGooglePayConfigurationBuilder().build()

        val paymentMethod = PaymentMethod(
            brands = listOf("mc", "amex", "maestro", "discover")
        )

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, paymentMethod)

        val expected = getGooglePayComponentParams(
            allowedCardNetworks = listOf("MASTERCARD", "AMEX", "DISCOVER")
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is explicitly set then its value shouldn't change`() {
        val googlePayConfiguration =
            getGooglePayConfigurationBuilder().setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION).build()

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is TEST then google pay environment should be ENVIRONMENT_TEST`() {
        val googlePayConfiguration = getGooglePayConfigurationBuilder().build()

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is a live one then google pay environment should be ENVIRONMENT_PRODUCTION`() {
        val googlePayConfiguration = GooglePayConfiguration.Builder(
            shopperLocale = Locale.CHINA, environment = Environment.UNITED_STATES, clientKey = TEST_CLIENT_KEY_2
        ).setMerchantAccount(TEST_GATEWAY_MERCHANT_ID).build()

        val params = GooglePayComponentParamsMapper(null).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is not set in parent configuration and google pay configuration then params amount should have 0 USD DEFAULT_VALUE`() {
        val googlePayConfiguration = getGooglePayConfigurationBuilder().build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY
        )

        val params = GooglePayComponentParamsMapper(overrideParams).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set with empty amount then params amount should have 0 USD DEFAULT_VALUE`() {
        // Google Pay Config is set with an amount which will be overridden by parent configuration
        val googlePayConfiguration = getGooglePayConfigurationBuilder()
            .setAmount(
                Amount(
                    currency = "TRY",
                    value = 40_00L
                )
            )
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        // parent configuration overrides amount to be Amount.EMPTY
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY
        )

        val params = GooglePayComponentParamsMapper(overrideParams).mapToParams(googlePayConfiguration, PaymentMethod())

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
        )

        assertEquals(expected, params)
    }

    private fun getGooglePayConfigurationBuilder() = GooglePayConfiguration.Builder(
        shopperLocale = Locale.US, environment = Environment.TEST, clientKey = TEST_CLIENT_KEY_1
    ).setMerchantAccount(TEST_GATEWAY_MERCHANT_ID)

    private fun getGooglePayComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        isAnalyticsEnabled: Boolean = true,
        isCreatedByDropIn: Boolean = false,
        gatewayMerchantId: String = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment: Int = WalletConstants.ENVIRONMENT_TEST,
        amount: Amount = Amount("USD", 0),
        totalPriceStatus: String = "FINAL",
        countryCode: String? = null,
        merchantInfo: MerchantInfo? = null,
        allowedAuthMethods: List<String> = AllowedAuthMethods.allAllowedAuthMethods,
        allowedCardNetworks: List<String> = AllowedCardNetworks.allAllowedCardNetworks,
        isAllowPrepaidCards: Boolean = false,
        isEmailRequired: Boolean = false,
        isExistingPaymentMethodRequired: Boolean = false,
        isShippingAddressRequired: Boolean = false,
        shippingAddressParameters: ShippingAddressParameters? = null,
        isBillingAddressRequired: Boolean = false,
        billingAddressParameters: BillingAddressParameters? = null,
    ) = GooglePayComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled,
        isCreatedByDropIn = isCreatedByDropIn,
        gatewayMerchantId = gatewayMerchantId,
        googlePayEnvironment = googlePayEnvironment,
        amount = amount,
        totalPriceStatus = totalPriceStatus,
        countryCode = countryCode,
        merchantInfo = merchantInfo,
        allowedAuthMethods = allowedAuthMethods,
        allowedCardNetworks = allowedCardNetworks,
        isAllowPrepaidCards = isAllowPrepaidCards,
        isEmailRequired = isEmailRequired,
        isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
        isShippingAddressRequired = isShippingAddressRequired,
        shippingAddressParameters = shippingAddressParameters,
        isBillingAddressRequired = isBillingAddressRequired,
        billingAddressParameters = billingAddressParameters,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val TEST_GATEWAY_MERCHANT_ID = "TEST_GATEWAY_MERCHANT_ID"
    }
}
