package com.adyen.checkout.googlepay

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.google.android.gms.wallet.WalletConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

internal class GooglePayConfigurationTest {

    @Test
    fun `when creating the configuration through CheckoutConfiguration, then it should be the same as when the builder is used`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        ) {
            googlePay {
                setSubmitButtonVisible(true)
                setMerchantAccount("merchantAccount")
                setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                setMerchantInfo(MerchantInfo(merchantId = "id"))
                setCountryCode("US")
                setAllowedAuthMethods(listOf(AllowedAuthMethods.PAN_ONLY))
                setAllowedCardNetworks(listOf(AllowedCardNetworks.VISA))
                allowedIssuerCountryCodes = listOf("US")
                setAllowPrepaidCards(true)
                setAllowCreditCards(false)
                setAssuranceDetailsRequired(true)
                setEmailRequired(true)
                setExistingPaymentMethodRequired(true)
                setShippingAddressRequired(true)
                setShippingAddressParameters(ShippingAddressParameters(isPhoneNumberRequired = true))
                setBillingAddressRequired(true)
                setBillingAddressParameters(BillingAddressParameters(format = "format"))
                setTotalPriceStatus("status")
            }
        }

        val actual = checkoutConfiguration.getGooglePayConfiguration()

        val expected = GooglePayConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setSubmitButtonVisible(true)
            .setAmount(Amount("EUR", 123L))
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setMerchantAccount("merchantAccount")
            .setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .setMerchantInfo(MerchantInfo(merchantId = "id"))
            .setCountryCode("US")
            .setAllowedAuthMethods(listOf(AllowedAuthMethods.PAN_ONLY))
            .setAllowedCardNetworks(listOf(AllowedCardNetworks.VISA))
            .setAllowedIssuerCountryCodes(listOf("US"))
            .setAllowPrepaidCards(true)
            .setAllowCreditCards(false)
            .setAssuranceDetailsRequired(true)
            .setEmailRequired(true)
            .setExistingPaymentMethodRequired(true)
            .setShippingAddressRequired(true)
            .setShippingAddressParameters(ShippingAddressParameters(isPhoneNumberRequired = true))
            .setBillingAddressRequired(true)
            .setBillingAddressParameters(BillingAddressParameters(format = "format"))
            .setTotalPriceStatus("status")
            .build()

        assertEquals(expected.shopperLocale, actual?.shopperLocale)
        assertEquals(expected.environment, actual?.environment)
        assertEquals(expected.clientKey, actual?.clientKey)
        assertEquals(expected.amount, actual?.amount)
        assertEquals(expected.analyticsConfiguration, actual?.analyticsConfiguration)
        assertEquals(expected.merchantAccount, actual?.merchantAccount)
        assertEquals(expected.googlePayEnvironment, actual?.googlePayEnvironment)
        assertEquals(expected.merchantInfo, actual?.merchantInfo)
        assertEquals(expected.countryCode, actual?.countryCode)
        assertEquals(expected.allowedAuthMethods, actual?.allowedAuthMethods)
        assertEquals(expected.allowedCardNetworks, actual?.allowedCardNetworks)
        assertEquals(expected.isAllowPrepaidCards, actual?.isAllowPrepaidCards)
        assertEquals(expected.isAllowCreditCards, actual?.isAllowCreditCards)
        assertEquals(expected.isAssuranceDetailsRequired, actual?.isAssuranceDetailsRequired)
        assertEquals(expected.isEmailRequired, actual?.isEmailRequired)
        assertEquals(expected.isExistingPaymentMethodRequired, actual?.isExistingPaymentMethodRequired)
        assertEquals(expected.isShippingAddressRequired, actual?.isShippingAddressRequired)
        assertEquals(expected.shippingAddressParameters, actual?.shippingAddressParameters)
        assertEquals(expected.isBillingAddressRequired, actual?.isBillingAddressRequired)
        assertEquals(expected.billingAddressParameters, actual?.billingAddressParameters)
        assertEquals(expected.totalPriceStatus, actual?.totalPriceStatus)
    }

    @Test
    fun `when the configuration is mapped to CheckoutConfiguration, then CheckoutConfiguration is created correctly`() {
        val config = GooglePayConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
            .setAmount(Amount("EUR", 123L))
            .setSubmitButtonVisible(true)
            .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            .setMerchantAccount("merchantAccount")
            .setGooglePayEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .setMerchantInfo(MerchantInfo(merchantId = "id"))
            .setCountryCode("US")
            .setAllowedAuthMethods(listOf(AllowedAuthMethods.PAN_ONLY))
            .setAllowedCardNetworks(listOf(AllowedCardNetworks.VISA))
            .setBlockedIssuerCountryCodes(listOf("US"))
            .setAllowPrepaidCards(true)
            .setAllowCreditCards(false)
            .setAssuranceDetailsRequired(true)
            .setEmailRequired(true)
            .setExistingPaymentMethodRequired(true)
            .setShippingAddressRequired(true)
            .setShippingAddressParameters(ShippingAddressParameters(isPhoneNumberRequired = true))
            .setBillingAddressRequired(true)
            .setBillingAddressParameters(BillingAddressParameters(format = "format"))
            .setTotalPriceStatus("status")
            .build()

        val actual = config.toCheckoutConfiguration()

        val expected = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            amount = Amount("EUR", 123L),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
        )

        assertEquals(expected.shopperLocale, actual.shopperLocale)
        assertEquals(expected.environment, actual.environment)
        assertEquals(expected.clientKey, actual.clientKey)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.analyticsConfiguration, actual.analyticsConfiguration)

        val actualGooglePayCardConfig = actual.getGooglePayConfiguration()
        assertEquals(config.shopperLocale, actualGooglePayCardConfig?.shopperLocale)
        assertEquals(config.environment, actualGooglePayCardConfig?.environment)
        assertEquals(config.clientKey, actualGooglePayCardConfig?.clientKey)
        assertEquals(config.amount, actualGooglePayCardConfig?.amount)
        assertEquals(config.isSubmitButtonVisible, actualGooglePayCardConfig?.isSubmitButtonVisible)
        assertEquals(config.analyticsConfiguration, actualGooglePayCardConfig?.analyticsConfiguration)
        assertEquals(config.merchantAccount, actualGooglePayCardConfig?.merchantAccount)
        assertEquals(config.googlePayEnvironment, actualGooglePayCardConfig?.googlePayEnvironment)
        assertEquals(config.merchantInfo, actualGooglePayCardConfig?.merchantInfo)
        assertEquals(config.countryCode, actualGooglePayCardConfig?.countryCode)
        assertEquals(config.allowedAuthMethods, actualGooglePayCardConfig?.allowedAuthMethods)
        assertEquals(config.allowedCardNetworks, actualGooglePayCardConfig?.allowedCardNetworks)
        assertEquals(config.allowedIssuerCountryCodes, actualGooglePayCardConfig?.allowedIssuerCountryCodes)
        assertEquals(config.blockedIssuerCountryCodes, actualGooglePayCardConfig?.blockedIssuerCountryCodes)
        assertEquals(config.isAllowPrepaidCards, actualGooglePayCardConfig?.isAllowPrepaidCards)
        assertEquals(config.isAllowCreditCards, actualGooglePayCardConfig?.isAllowCreditCards)
        assertEquals(config.isAssuranceDetailsRequired, actualGooglePayCardConfig?.isAssuranceDetailsRequired)
        assertEquals(config.isEmailRequired, actualGooglePayCardConfig?.isEmailRequired)
        assertEquals(config.isExistingPaymentMethodRequired, actualGooglePayCardConfig?.isExistingPaymentMethodRequired)
        assertEquals(config.isShippingAddressRequired, actualGooglePayCardConfig?.isShippingAddressRequired)
        assertEquals(config.shippingAddressParameters, actualGooglePayCardConfig?.shippingAddressParameters)
        assertEquals(config.isBillingAddressRequired, actualGooglePayCardConfig?.isBillingAddressRequired)
        assertEquals(config.billingAddressParameters, actualGooglePayCardConfig?.billingAddressParameters)
        assertEquals(config.totalPriceStatus, actualGooglePayCardConfig?.totalPriceStatus)
    }

    @Test
    fun `when allowedIssuerCountryCodes and blockedIssuerCountryCodes are both set, then an exception is thrown`() {
        assertThrows<CheckoutException> {
            CheckoutConfiguration(
                shopperLocale = Locale.US,
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY,
                amount = Amount("EUR", 123L),
                analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
            ) {
                googlePay {
                    allowedIssuerCountryCodes = listOf("US")
                    blockedIssuerCountryCodes = listOf("US")
                }
            }
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
