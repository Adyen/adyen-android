/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import com.adyen.checkout.googlepay.googlePay
import com.google.android.gms.wallet.WalletConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class GooglePayComponentParamsMapperTest {

    @BeforeEach
    fun beforeEach() {
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Test
    fun `when parent configuration is null and custom google pay configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params =
            GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)

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

        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = amount,
        ) {
            googlePay {
                setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                setMerchantAccount("MERCHANT_ACCOUNT")
                setAllowPrepaidCards(true)
                setAllowCreditCards(true)
                setAssuranceDetailsRequired(true)
                setCountryCode("ZZ")
                setMerchantInfo(merchantInfo)
                setAllowedAuthMethods(allowedAuthMethods)
                setAllowedCardNetworks(allowedCardNetworks)
                setBillingAddressParameters(billingAddressParameters)
                setBillingAddressRequired(true)
                setEmailRequired(true)
                setExistingPaymentMethodRequired(true)
                setShippingAddressParameters(shippingAddressParameters)
                setShippingAddressRequired(true)
                setTotalPriceStatus("STATUS")
            }
        }

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)

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
            isAllowCreditCards = true,
            isAssuranceDetailsRequired = true,
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
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "XCD",
                value = 4_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            googlePay {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
                setMerchantAccount(TEST_GATEWAY_MERCHANT_ID)
            }
        }

        val params = GooglePayComponentParamsMapper(true, null).mapToParams(
            configuration,
            PaymentMethod(),
            null,
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "XCD",
                value = 4_00L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is set in googlePayConfiguration then it takes priority over gatewayMerchantId in the paymentMethod configuration`() {
        val configuration = createCheckoutConfiguration {
            setMerchantAccount("GATEWAY_MERCHANT_ID_1")
        }

        val paymentMethod = PaymentMethod(
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, paymentMethod, null)

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_1",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is not set in googlePayConfiguration then gatewayMerchantId in the paymentMethod configuration is used`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
        ) {
            googlePay()
        }

        val paymentMethod = PaymentMethod(
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, paymentMethod, null)

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when neither merchantAccount in googlePayConfiguration nor gatewayMerchantId in the paymentMethod configuration is set then exception is thrown`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
        ) {
            googlePay()
        }

        assertThrows<ComponentException> {
            GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)
        }
    }

    @Test
    fun `when allowedCardNetworks is not set in googlePayConfiguration then brands in the paymentMethod is used`() {
        val configuration = createCheckoutConfiguration()

        val paymentMethod = PaymentMethod(
            brands = listOf("mc", "amex", "maestro", "discover"),
        )

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, paymentMethod, null)

        val expected = getGooglePayComponentParams(
            allowedCardNetworks = listOf("MASTERCARD", "AMEX", "DISCOVER"),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is explicitly set then its value shouldn't change`() {
        val configuration = createCheckoutConfiguration {
            setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
        }

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is TEST then google pay environment should be ENVIRONMENT_TEST`() {
        val configuration = createCheckoutConfiguration()

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is a live one then google pay environment should be ENVIRONMENT_PRODUCTION`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            googlePay {
                setMerchantAccount(TEST_GATEWAY_MERCHANT_ID)
            }
        }

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(configuration, PaymentMethod(), null)

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is not set in parent configuration and google pay configuration then params amount should have 0 USD DEFAULT_VALUE`() {
        val configuration = createCheckoutConfiguration(amount = null)

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(
            configuration,
            PaymentMethod(),
            null,
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set with empty amount then params amount should have 0 USD DEFAULT_VALUE`() {
        // Google Pay Config is set with an amount which will be overridden by parent configuration
        val configuration = createCheckoutConfiguration(amount = null) {
            setAmount(Amount(currency = "TRY", value = 40_00L))
        }

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(
            configuration,
            PaymentMethod(),
            null,
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
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

        val params = GooglePayComponentParamsMapper(false, null).mapToParams(
            configuration = configuration,
            paymentMethod = PaymentMethod(),
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
            ),
        )

        val expected = getGooglePayComponentParams(
            amount = expectedValue,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: GooglePayConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        googlePay {
            setMerchantAccount(TEST_GATEWAY_MERCHANT_ID)
            apply(configuration)
        }
    }

    @Suppress("LongParameterList")
    private fun getGooglePayComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
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
        isAllowCreditCards: Boolean? = null,
        isAssuranceDetailsRequired: Boolean? = null,
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
        analyticsParams = analyticsParams,
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
        isAllowCreditCards = isAllowCreditCards,
        isAssuranceDetailsRequired = isAssuranceDetailsRequired,
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

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), null, Amount("EUR", 100)),
        )
    }
}
