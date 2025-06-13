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
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import com.adyen.checkout.googlepay.googlePay
import com.adyen.checkout.test.LoggingExtension
import com.google.android.gms.wallet.WalletConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

@ExtendWith(LoggingExtension::class)
internal class GooglePayComponentParamsMapperTest {

    private val googlePayComponentParamsMapper = GooglePayComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom google pay configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom google pay configuration fields are set then all fields should match`() {
        val amount = Amount("EUR", 1337)
        val merchantInfo = MerchantInfo("MERCHANT_NAME", "MERCHANT_ID")
        val allowedAuthMethods = listOf("AUTH1", "AUTH2", "AUTH3")
        val allowedCardNetworks = listOf("CARD1", "CARD2")
        val shippingAddressParameters = ShippingAddressParameters(listOf("ZZ", "AA"), true)
        val billingAddressParameters = BillingAddressParameters("FORMAT", true)
        val googlePayButtonStyling = GooglePayButtonStyling(
            buttonTheme = GooglePayButtonTheme.LIGHT,
            buttonType = GooglePayButtonType.BOOK,
            cornerRadius = 16,
        )

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
                setCheckoutOption("DEFAULT")
                setGooglePayButtonStyling(googlePayButtonStyling)
            }
        }

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_2),
            amount = amount,
            isSubmitButtonVisible = false,
            gatewayMerchantId = "MERCHANT_ACCOUNT",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
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
            checkoutOption = "DEFAULT",
            googlePayButtonStyling = googlePayButtonStyling,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override google pay configuration fields`() {
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
                setSubmitButtonVisible(true)
                setMerchantAccount(TEST_GATEWAY_MERCHANT_ID)
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.INITIAL, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
            isSubmitButtonVisible = false,
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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_1",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is not set in googlePayConfiguration then gatewayMerchantId in the paymentMethod configuration is used`() {
        val configuration = CheckoutConfiguration(
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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when neither merchantAccount in googlePayConfiguration nor gatewayMerchantId in the paymentMethod configuration is set then exception is thrown`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
        ) {
            googlePay()
        }

        assertThrows<ComponentException> {
            googlePayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
                paymentMethod = PaymentMethod(),
            )
        }
    }

    @Test
    fun `when allowedCardNetworks is not set in googlePayConfiguration then brands in the paymentMethod is used`() {
        val configuration = createCheckoutConfiguration()

        val paymentMethod = PaymentMethod(
            brands = listOf("mc", "amex", "maestro", "discover"),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = paymentMethod,
        )

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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is TEST then google pay environment should be ENVIRONMENT_TEST`() {
        val configuration = createCheckoutConfiguration()

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_2),
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is not set in checkout configuration and google pay configuration then params amount should have 0 USD DEFAULT_VALUE`() {
        val configuration = createCheckoutConfiguration(amount = null)

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
            isCreatedByDropIn = false,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when checkout configuration is used with null amount then params amount should have 0 USD DEFAULT_VALUE`() {
        // Google Pay Config is set with an amount which will be overridden by checkout configuration params
        val configuration = createCheckoutConfiguration(amount = null) {
            setAmount(Amount(currency = "TRY", value = 40_00L))
        }

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
            isCreatedByDropIn = false,
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
        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
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

        val params = googlePayComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when created by drop-in, then submit button should not be visible`() {
            val configuration = createCheckoutConfiguration()

            val params = googlePayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = DropInOverrideParams(null, null, true),
                componentSessionParams = null,
                paymentMethod = PaymentMethod(),
            )

            assertFalse(params.isSubmitButtonVisible)
        }

        @Test
        fun `when not created by drop-in and set in configuration, then submit button should be visible`() {
            val configuration = createCheckoutConfiguration {
                setSubmitButtonVisible(true)
            }

            val params = googlePayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
                paymentMethod = PaymentMethod(),
            )

            assertTrue(params.isSubmitButtonVisible)
        }

        @Test
        fun `when not created by drop-in and not configured, then submit button should not be visible`() {
            val configuration = createCheckoutConfiguration()

            val params = googlePayComponentParamsMapper.mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = DEVICE_LOCALE,
                dropInOverrideParams = null,
                componentSessionParams = null,
                paymentMethod = PaymentMethod(),
            )

            assertFalse(params.isSubmitButtonVisible)
        }
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: GooglePayConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
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
    private fun getGooglePayComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = false,
        gatewayMerchantId: String = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment: Int = WalletConstants.ENVIRONMENT_TEST,
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
        checkoutOption: String? = null,
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ) = GooglePayComponentParams(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        amount = amount ?: Amount("USD", 0),
        isSubmitButtonVisible = isSubmitButtonVisible,
        gatewayMerchantId = gatewayMerchantId,
        googlePayEnvironment = googlePayEnvironment,
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
        checkoutOption = checkoutOption,
        googlePayButtonStyling = googlePayButtonStyling,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val TEST_GATEWAY_MERCHANT_ID = "TEST_GATEWAY_MERCHANT_ID"
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
