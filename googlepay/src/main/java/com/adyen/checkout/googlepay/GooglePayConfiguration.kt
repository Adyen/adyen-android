/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
@file:Suppress("unused")

package com.adyen.checkout.googlepay

import android.content.Context
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.action.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.google.android.gms.wallet.WalletConstants
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [GooglePayComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class GooglePayConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    val merchantAccount: String?,
    val googlePayEnvironment: Int?,
    override val amount: Amount,
    val totalPriceStatus: String?,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>?,
    val allowedCardNetworks: List<String>?,
    val isAllowPrepaidCards: Boolean?,
    val isEmailRequired: Boolean?,
    val isExistingPaymentMethodRequired: Boolean?,
    val isShippingAddressRequired: Boolean?,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean?,
    val billingAddressParameters: BillingAddressParameters?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration {

    /**
     * Builder to create a [GooglePayConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<GooglePayConfiguration, Builder> {
        private var merchantAccount: String? = null
        private var googlePayEnvironment: Int? = null
        private var merchantInfo: MerchantInfo? = null
        private var countryCode: String? = null
        private var allowedAuthMethods: List<String>? = null
        private var allowedCardNetworks: List<String>? = null
        private var isAllowPrepaidCards: Boolean? = null
        private var isEmailRequired: Boolean? = null
        private var isExistingPaymentMethodRequired: Boolean? = null
        private var isShippingAddressRequired: Boolean? = null
        private var shippingAddressParameters: ShippingAddressParameters? = null
        private var isBillingAddressRequired: Boolean? = null
        private var billingAddressParameters: BillingAddressParameters? = null
        private var totalPriceStatus: String? = null

        /**
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A context
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        /**
         * Initialize a configuration builder with the required fields.
         *
         * @param shopperLocale The [Locale] of the shopper.
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Set the merchant account to be put in the payment token from Google to Adyen.
         *
         * If not set then [PaymentMethod.configuration.gatewayMerchantId] will be used.
         * If that value is also not set, an exception will be thrown indicating that you need to update you Adyen API
         * version or pass this value manually.
         *
         * @param merchantAccount Your merchant account.
         */
        fun setMerchantAccount(merchantAccount: String): Builder {
            this.merchantAccount = merchantAccount
            return this
        }

        /**
         * Sets the environment to be used by GooglePay.
         * Should be either [WalletConstants.ENVIRONMENT_TEST] or [WalletConstants.ENVIRONMENT_PRODUCTION].
         *
         * Default follows the value of the Adyen [environment].
         *
         * @param googlePayEnvironment The GooglePay environment.
         */
        fun setGooglePayEnvironment(googlePayEnvironment: Int): Builder {
            if (!isGooglePayEnvironmentValid(googlePayEnvironment)) {
                throw CheckoutException(
                    "Invalid value for Google Environment. Use either WalletConstants.ENVIRONMENT_TEST or" +
                        " WalletConstants.ENVIRONMENT_PRODUCTION"
                )
            }
            this.googlePayEnvironment = googlePayEnvironment
            return this
        }

        private fun isGooglePayEnvironmentValid(environment: Int): Boolean =
            environment == WalletConstants.ENVIRONMENT_TEST || environment == WalletConstants.ENVIRONMENT_PRODUCTION

        /**
         * Sets the information about the merchant requesting the payment.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
         * for more details.
         *
         */
        fun setMerchantInfo(merchantInfo: MerchantInfo?): Builder {
            this.merchantInfo = merchantInfo
            return this
        }

        /**
         * Sets the ISO 3166-1 alpha-2 country code where the transaction is processed.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo)
         * for more details.
         *
         */
        fun setCountryCode(countryCode: String?): Builder {
            this.countryCode = countryCode
            return this
        }

        /**
         * Sets the supported authentication methods.
         *
         * Default is ["PAN_ONLY", "CRYPTOGRAM_3DS"].
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        fun setAllowedAuthMethods(allowedAuthMethods: List<String>?): Builder {
            this.allowedAuthMethods = allowedAuthMethods
            return this
        }

        /**
         * Sets the allowed card networks. The allowed networks are automatically configured based on your account
         * settings, but you can override them here.
         *
         * Default is ["AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA"].
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        fun setAllowedCardNetworks(allowedCardNetworks: List<String>?): Builder {
            this.allowedCardNetworks = allowedCardNetworks
            return this
        }

        /**
         * Set if you support prepaid cards.
         *
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        fun setAllowPrepaidCards(isAllowPrepaidCards: Boolean): Builder {
            this.isAllowPrepaidCards = isAllowPrepaidCards
            return this
        }

        /**
         * Set to true if you require an email address.
         *
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
         * for more details.
         *
         */
        fun setEmailRequired(isEmailRequired: Boolean): Builder {
            this.isEmailRequired = isEmailRequired
            return this
        }

        /**
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#IsReadyToPayRequest)
         * for more details.
         *
         */
        @Suppress("MaxLineLength")
        fun setExistingPaymentMethodRequired(isExistingPaymentMethodRequired: Boolean): Builder {
            this.isExistingPaymentMethodRequired = isExistingPaymentMethodRequired
            return this
        }

        /**
         * Set to true if you require a shipping address.
         *
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
         * for more details.
         *
         */
        fun setShippingAddressRequired(isShippingAddressRequired: Boolean): Builder {
            this.isShippingAddressRequired = isShippingAddressRequired
            return this
        }

        /**
         * Sets the required shipping address details.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
         * for more details.
         *
         */
        fun setShippingAddressParameters(shippingAddressParameters: ShippingAddressParameters?): Builder {
            this.shippingAddressParameters = shippingAddressParameters
            return this
        }

        /**
         * Set to true if you require a billing address.
         *
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        fun setBillingAddressRequired(isBillingAddressRequired: Boolean): Builder {
            this.isBillingAddressRequired = isBillingAddressRequired
            return this
        }

        /**
         * Sets the required billing address details.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        fun setBillingAddressParameters(billingAddressParameters: BillingAddressParameters?): Builder {
            this.billingAddressParameters = billingAddressParameters
            return this
        }

        /**
         * Sets the status of the total price used.
         *
         * Default is "FINAL".
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo)
         * for more details.
         *
         */
        fun setTotalPriceStatus(totalPriceStatus: String): Builder {
            this.totalPriceStatus = totalPriceStatus
            return this
        }

        /**
         * Sets the amount of the transaction.
         *
         * Default is 0 USD.
         *
         * Check the totalPrice field in the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo)
         * for more details.
         *
         * @param amount Amount of the transaction.
         */
        override fun setAmount(amount: Amount): Builder {
            return super.setAmount(amount)
        }

        override fun buildInternal(): GooglePayConfiguration {
            return GooglePayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                merchantAccount = merchantAccount,
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
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}
