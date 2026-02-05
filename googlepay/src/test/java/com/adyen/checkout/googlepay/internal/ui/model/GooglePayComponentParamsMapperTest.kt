/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.error.internal.ComponentError
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.GooglePayConfiguration
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
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration()

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
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

        val componentParamsBundle = createComponentParamsBundle(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = amount,
        )

        val googlePayConfiguration = createGooglePayConfiguration(
            merchantAccount = "MERCHANT_ACCOUNT",
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

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            amount = amount,
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
    fun `when merchantAccount is set in googlePayConfiguration then it takes priority over gatewayMerchantId in the paymentMethod configuration`() {
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration(
            merchantAccount = "GATEWAY_MERCHANT_ID_1",
        )

        val paymentMethod = PaymentMethod(
            configuration = com.adyen.checkout.components.core.Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_1",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when merchantAccount is not set in googlePayConfiguration then gatewayMerchantId in the paymentMethod configuration is used`() {
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration(
            merchantAccount = null,
        )

        val paymentMethod = PaymentMethod(
            configuration = com.adyen.checkout.components.core.Configuration(
                gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
            ),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            gatewayMerchantId = "GATEWAY_MERCHANT_ID_2",
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when neither merchantAccount in googlePayConfiguration nor gatewayMerchantId in the paymentMethod configuration is set then exception is thrown`() {
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration(
            merchantAccount = null,
        )

        assertThrows<ComponentError> {
            googlePayComponentParamsMapper.mapToParams(
                componentParamsBundle = componentParamsBundle,
                googlePayConfiguration = googlePayConfiguration,
                paymentMethod = PaymentMethod(),
            )
        }
    }

    @Test
    fun `when allowedCardNetworks is not set in googlePayConfiguration then brands in the paymentMethod is used`() {
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration(
            allowedCardNetworks = null,
        )

        val paymentMethod = PaymentMethod(
            brands = listOf("mc", "amex", "maestro", "discover"),
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = paymentMethod,
        )

        val expected = getGooglePayComponentParams(
            allowedCardNetworks = listOf("MASTERCARD", "AMEX", "DISCOVER"),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is explicitly set then its value should not change`() {
        val componentParamsBundle = createComponentParamsBundle()
        val googlePayConfiguration = createGooglePayConfiguration(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is TEST then google pay environment should be ENVIRONMENT_TEST`() {
        val componentParamsBundle = createComponentParamsBundle(
            environment = Environment.TEST,
        )
        val googlePayConfiguration = createGooglePayConfiguration(
            googlePayEnvironment = null,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            environment = Environment.TEST,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when google pay environment is not set and environment is a live one then google pay environment should be ENVIRONMENT_PRODUCTION`() {
        val componentParamsBundle = createComponentParamsBundle(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
        )
        val googlePayConfiguration = createGooglePayConfiguration(
            googlePayEnvironment = null,
        )

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            shopperLocale = Locale.CHINA,
            environment = Environment.UNITED_STATES,
            clientKey = TEST_CLIENT_KEY_2,
            googlePayEnvironment = WalletConstants.ENVIRONMENT_PRODUCTION,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is not set in componentParamsBundle then params amount should have 0 USD default value`() {
        val componentParamsBundle = createComponentParamsBundle(
            amount = null,
        )
        val googlePayConfiguration = createGooglePayConfiguration()

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            amount = null,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when amount is set in componentParamsBundle then params amount should match`() {
        val amount = Amount("EUR", 1000)
        val componentParamsBundle = createComponentParamsBundle(
            amount = amount,
        )
        val googlePayConfiguration = createGooglePayConfiguration()

        val params = googlePayComponentParamsMapper.mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = PaymentMethod(),
        )

        val expected = getGooglePayComponentParams(
            amount = amount,
        )

        assertEquals(expected, params)
    }

    @Suppress("LongParameterList")
    private fun createComponentParamsBundle(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        showSubmitButton: Boolean = false,
        publicKey: String? = null,
    ) = ComponentParamsBundle(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
            showSubmitButton = showSubmitButton,
            publicKey = publicKey,
        ),
        sessionParams = null,
    )

    @Suppress("LongParameterList")
    private fun createGooglePayConfiguration(
        merchantAccount: String? = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment: Int? = null,
        totalPriceStatus: String? = null,
        countryCode: String? = null,
        merchantInfo: MerchantInfo? = null,
        allowedAuthMethods: List<String>? = null,
        allowedCardNetworks: List<String>? = null,
        isAllowPrepaidCards: Boolean? = null,
        isAllowCreditCards: Boolean? = null,
        isAssuranceDetailsRequired: Boolean? = null,
        isEmailRequired: Boolean? = null,
        isExistingPaymentMethodRequired: Boolean? = null,
        isShippingAddressRequired: Boolean? = null,
        shippingAddressParameters: ShippingAddressParameters? = null,
        isBillingAddressRequired: Boolean? = null,
        billingAddressParameters: BillingAddressParameters? = null,
        checkoutOption: String? = null,
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ) = GooglePayConfiguration(
        merchantAccount = merchantAccount,
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

    @Suppress("LongParameterList")
    private fun getGooglePayComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        showSubmitButton: Boolean = false,
        publicKey: String? = null,
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
            showSubmitButton = showSubmitButton,
            publicKey = publicKey,
        ),
        amount = amount ?: Amount("USD", 0),
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
    }
}
