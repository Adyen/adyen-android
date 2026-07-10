/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.Configuration
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.GooglePayPaymentMethodParameters
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import com.google.android.gms.wallet.WalletConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@ExtendWith(LoggingExtension::class)
internal class GooglePayComponentParamsMapperTest {

    private val googlePayComponentParamsMapper = GooglePayComponentParamsMapper()

    @Test
    fun `when custom google pay configuration fields are null then all fields should match defaults`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when custom google pay configuration fields are set then all fields should match`() {
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

        val cardParameters = GooglePayPaymentMethodParameters.Card(
            allowedAuthMethods = allowedAuthMethods,
            allowedCardNetworks = allowedCardNetworks,
            allowPrepaidCards = true,
            allowCreditCards = true,
            assuranceDetailsRequired = true,
            billingAddressRequired = true,
            billingAddressParameters = billingAddressParameters,
        )

        val googlePayConfiguration = createGooglePayConfiguration(
            merchantAccount = "MERCHANT_ACCOUNT",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            totalPriceStatus = "STATUS",
            countryCode = "ZZ",
            merchantInfo = merchantInfo,
            allowedPaymentMethods = listOf(cardParameters),
            isEmailRequired = true,
            isExistingPaymentMethodRequired = true,
            isShippingAddressRequired = true,
            shippingAddressParameters = shippingAddressParameters,
            checkoutOption = "DEFAULT",
            googlePayButtonStyling = googlePayButtonStyling,
        )

        val checkoutParams = createCheckoutParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.LIVE_APSE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = amount,
            configuration = googlePayConfiguration,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            amount = amount,
            gatewayMerchantId = "MERCHANT_ACCOUNT",
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            totalPriceStatus = "STATUS",
            countryCode = "ZZ",
            merchantInfo = merchantInfo,
            allowedPaymentMethods = listOf(
                resolvedCard(
                    allowedAuthMethods = allowedAuthMethods,
                    allowedCardNetworks = allowedCardNetworks,
                    isAllowPrepaidCards = true,
                    isAllowCreditCards = true,
                    isAssuranceDetailsRequired = true,
                    isBillingAddressRequired = true,
                    billingAddressParameters = billingAddressParameters,
                ),
            ),
            isEmailRequired = true,
            isExistingPaymentMethodRequired = true,
            isShippingAddressRequired = true,
            shippingAddressParameters = shippingAddressParameters,
            checkoutOption = "DEFAULT",
            googlePayButtonStyling = googlePayButtonStyling,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is set in googlePayConfiguration then it takes priority over gatewayMerchantId in the paymentMethod configuration`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                merchantAccount = "GATEWAY_MERCHANT_ID_1",
            ),
        )

        val paymentMethod = createPaymentMethod(
            brands = emptyList(),
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_1",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is not set in googlePayConfiguration then gatewayMerchantId in the paymentMethod configuration is used`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                merchantAccount = null,
            ),
        )

        val paymentMethod = createPaymentMethod(
            brands = emptyList(),
            configuration = Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when neither merchantAccount in googlePayConfiguration nor gatewayMerchantId in the paymentMethod configuration is set then exception is thrown`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                merchantAccount = null,
            ),
        )

        assertThrows<InternalCheckoutError> {
            googlePayComponentParamsMapper.mapToParams(
                params = checkoutParams,
                paymentMethod = createPaymentMethod(),
            )
        }
    }

    @Test
    fun `when allowedCardNetworks is not set in googlePayConfiguration then brands in the paymentMethod is used`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                allowedPaymentMethods = listOf(GooglePayPaymentMethodParameters.Card()),
            ),
        )

        val paymentMethod = createPaymentMethod(
            brands = listOf("mc", "amex", "maestro", "discover"),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            allowedPaymentMethods = listOf(
                resolvedCard(
                    allowedCardNetworks = listOf("MASTERCARD", "AMEX", "DISCOVER"),
                ),
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when multiple payment methods are configured then all are resolved and preserved`() {
        val firstCard = GooglePayPaymentMethodParameters.Card(
            allowedAuthMethods = listOf("PAN_ONLY"),
            allowedCardNetworks = listOf("VISA"),
        )
        val secondCard = GooglePayPaymentMethodParameters.Card(
            allowedAuthMethods = listOf("CRYPTOGRAM_3DS"),
            allowedCardNetworks = listOf("MASTERCARD"),
        )

        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                allowedPaymentMethods = listOf(firstCard, secondCard),
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            allowedPaymentMethods = listOf(
                resolvedCard(
                    allowedAuthMethods = listOf("PAN_ONLY"),
                    allowedCardNetworks = listOf("VISA"),
                ),
                resolvedCard(
                    allowedAuthMethods = listOf("CRYPTOGRAM_3DS"),
                    allowedCardNetworks = listOf("MASTERCARD"),
                ),
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is explicitly set then its value should not change`() {
        val checkoutParams = createCheckoutParams(
            configuration = createGooglePayConfiguration(
                googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is TEST then google pay environment should be ENVIRONMENT_TEST`() {
        val checkoutParams = createCheckoutParams(
            environment = Environment.TEST,
            configuration = createGooglePayConfiguration(
                googlePayEnvironment = null,
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is a live one then google pay environment should be ENVIRONMENT_PRODUCTION`() {
        val checkoutParams = createCheckoutParams(
            shopperLocale = Locale.CHINA,
            environment = Environment.LIVE_UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
            configuration = createGooglePayConfiguration(
                googlePayEnvironment = null,
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is not set in checkoutParams then params amount should have 0 USD default value`() {
        val checkoutParams = createCheckoutParams(
            amount = null,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            amount = null,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is set in checkoutParams then params amount should match`() {
        val amount = Amount("EUR", 1000)
        val checkoutParams = createCheckoutParams(
            amount = amount,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            params = checkoutParams,
            paymentMethod = createPaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            amount = amount,
        )

        assertEquals(expected, params)
    }

    @Suppress("LongParameterList")
    private fun createCheckoutParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount: Amount? = null,
        showSubmitButton: Boolean = false,
        publicKey: String? = null,
        configuration: GooglePayConfiguration = createGooglePayConfiguration(),
    ) = CheckoutParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        amount = amount,
        showSubmitButton = showSubmitButton,
        publicKey = publicKey,
        additionalConfigurations = mapOf(GooglePayConfiguration::class.java.name to configuration),
        additionalSessionParams = null,
    )

    @Suppress("LongParameterList")
    private fun createGooglePayConfiguration(
        merchantAccount: String? = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment: Int? = null,
        totalPriceStatus: String? = null,
        countryCode: String? = null,
        merchantInfo: MerchantInfo? = null,
        allowedPaymentMethods: List<GooglePayPaymentMethodParameters>? = null,
        isEmailRequired: Boolean? = null,
        isExistingPaymentMethodRequired: Boolean? = null,
        isShippingAddressRequired: Boolean? = null,
        shippingAddressParameters: ShippingAddressParameters? = null,
        checkoutOption: String? = null,
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ) = GooglePayConfiguration(
        merchantAccount = merchantAccount,
        googlePayEnvironment = googlePayEnvironment,
        totalPriceStatus = totalPriceStatus,
        countryCode = countryCode,
        merchantInfo = merchantInfo,
        allowedPaymentMethods = allowedPaymentMethods,
        isEmailRequired = isEmailRequired,
        isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
        isShippingAddressRequired = isShippingAddressRequired,
        shippingAddressParameters = shippingAddressParameters,
        checkoutOption = checkoutOption,
        googlePayButtonStyling = googlePayButtonStyling,
    )

    @Suppress("LongParameterList")
    private fun getGooglePayComponentParams(
        amount: Amount? = null,
        gatewayMerchantId: String = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment: Int = WalletConstants.ENVIRONMENT_TEST,
        totalPriceStatus: String = "FINAL",
        countryCode: String? = null,
        merchantInfo: MerchantInfo? = null,
        allowedPaymentMethods: List<GooglePayPaymentMethodParams> = listOf(resolvedCard()),
        isEmailRequired: Boolean = false,
        isExistingPaymentMethodRequired: Boolean = false,
        isShippingAddressRequired: Boolean = false,
        shippingAddressParameters: ShippingAddressParameters? = null,
        checkoutOption: String? = null,
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ) = GooglePayComponentParams(
        amount = amount ?: Amount("USD", 0),
        gatewayMerchantId = gatewayMerchantId,
        googlePayEnvironment = googlePayEnvironment,
        totalPriceStatus = totalPriceStatus,
        countryCode = countryCode,
        merchantInfo = merchantInfo,
        allowedPaymentMethods = allowedPaymentMethods,
        isEmailRequired = isEmailRequired,
        isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
        isShippingAddressRequired = isShippingAddressRequired,
        shippingAddressParameters = shippingAddressParameters,
        checkoutOption = checkoutOption,
        googlePayButtonStyling = googlePayButtonStyling,
    )

    @Suppress("LongParameterList")
    private fun resolvedCard(
        allowedAuthMethods: List<String> = AllowedAuthMethods.allAllowedAuthMethods,
        allowedCardNetworks: List<String> = AllowedCardNetworks.allAllowedCardNetworks,
        isAllowPrepaidCards: Boolean = false,
        isAllowCreditCards: Boolean? = null,
        isAssuranceDetailsRequired: Boolean? = null,
        isBillingAddressRequired: Boolean = false,
        billingAddressParameters: BillingAddressParameters? = null,
    ) = GooglePayPaymentMethodParams.Card(
        allowedAuthMethods = allowedAuthMethods,
        allowedCardNetworks = allowedCardNetworks,
        isAllowPrepaidCards = isAllowPrepaidCards,
        isAllowCreditCards = isAllowCreditCards,
        isAssuranceDetailsRequired = isAssuranceDetailsRequired,
        isBillingAddressRequired = isBillingAddressRequired,
        billingAddressParameters = billingAddressParameters,
    )

    private fun createPaymentMethod(
        brands: List<String> = emptyList(),
        configuration: Configuration? = null,
    ) = GooglePayPaymentMethod(
        type = PaymentMethodTypes.GOOGLE_PAY,
        name = "Google Pay",
        brands = brands,
        configuration = configuration,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private const val TEST_GATEWAY_MERCHANT_ID = "TEST_GATEWAY_MERCHANT_ID"
        private val DEVICE_LOCALE = Locale.forLanguageTag("nl-NL")
    }
}
