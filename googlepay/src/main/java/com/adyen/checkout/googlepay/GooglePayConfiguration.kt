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
import com.adyen.checkout.components.base.AmountConfiguration
import com.adyen.checkout.components.base.AmountConfigurationBuilder
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.CheckoutCurrency.Companion.isSupported
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.googlepay.model.BillingAddressParameters
import com.adyen.checkout.googlepay.model.MerchantInfo
import com.adyen.checkout.googlepay.model.ShippingAddressParameters
import com.google.android.gms.wallet.WalletConstants
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
@Suppress("LongParameterList")
class GooglePayConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    val merchantAccount: String?,
    val googlePayEnvironment: Int,
    override val amount: Amount,
    val totalPriceStatus: String,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>?,
    val allowedCardNetworks: List<String>?,
    val isAllowPrepaidCards: Boolean,
    val isEmailRequired: Boolean,
    val isExistingPaymentMethodRequired: Boolean,
    val isShippingAddressRequired: Boolean,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean,
    val billingAddressParameters: BillingAddressParameters?,
) : Configuration, AmountConfiguration {

    /**
     * Builder to create a [GooglePayConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder : BaseConfigurationBuilder<GooglePayConfiguration>, AmountConfigurationBuilder {
        private var merchantAccount: String? = null
        private var googlePayEnvironment = getDefaultGooglePayEnvironment(environment)
        private var amount = Amount().apply {
            value = 0
            currency = CheckoutCurrency.USD.name
        }
        private var merchantInfo: MerchantInfo? = null
        private var countryCode: String? = null
        private var allowedAuthMethods: List<String>? = null
        private var allowedCardNetworks: List<String>? = null
        private var isAllowPrepaidCards = false
        private var isEmailRequired = false
        private var isExistingPaymentMethodRequired = false
        private var isShippingAddressRequired = false
        private var shippingAddressParameters: ShippingAddressParameters? = null
        private var isBillingAddressRequired = false
        private var billingAddressParameters: BillingAddressParameters? = null
        private var totalPriceStatus: String = "FINAL"

        private var isGoogleEnvironmentSetManually = false

        private fun getDefaultGooglePayEnvironment(environment: Environment): Int {
            return if (environment == Environment.TEST) {
                WalletConstants.ENVIRONMENT_TEST
            } else WalletConstants.ENVIRONMENT_PRODUCTION
        }

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The locale of the Shopper for translation.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey     Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: GooglePayConfiguration) : super(configuration) {
            merchantAccount = configuration.merchantAccount
            googlePayEnvironment = configuration.googlePayEnvironment
            amount = configuration.amount
            totalPriceStatus = configuration.totalPriceStatus
            countryCode = configuration.countryCode
            merchantInfo = configuration.merchantInfo
            allowedAuthMethods = configuration.allowedAuthMethods
            allowedCardNetworks = configuration.allowedCardNetworks
            isAllowPrepaidCards = configuration.isAllowPrepaidCards
            isEmailRequired = configuration.isEmailRequired
            isExistingPaymentMethodRequired = configuration.isExistingPaymentMethodRequired
            isShippingAddressRequired = configuration.isShippingAddressRequired
            shippingAddressParameters = configuration.shippingAddressParameters
            isBillingAddressRequired = configuration.isBillingAddressRequired
            billingAddressParameters = configuration.billingAddressParameters
        }

        override fun setEnvironment(environment: Environment): Builder {
            if (!isGoogleEnvironmentSetManually) {
                googlePayEnvironment = getDefaultGooglePayEnvironment(environment)
            }
            return super.setEnvironment(environment) as Builder
        }

        fun setTotalPriceStatus(totalPriceStatus: String) {
            this.totalPriceStatus = totalPriceStatus
        }

        override fun buildInternal(): GooglePayConfiguration {
            return GooglePayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
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
            )
        }

        /**
         * Set the merchant account to be put in the payment token from Google to Adyen.
         *
         * @param merchantAccount Your merchant account.
         */
        fun setMerchantAccount(merchantAccount: String): Builder {
            this.merchantAccount = merchantAccount
            return this
        }

        /**
         * Set the environment to be used by GooglePay.
         * Should be either [WalletConstants.ENVIRONMENT_TEST] or [WalletConstants.ENVIRONMENT_PRODUCTION]
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
            isGoogleEnvironmentSetManually = true
            return this
        }

        private fun isGooglePayEnvironmentValid(environment: Int): Boolean =
            environment == WalletConstants.ENVIRONMENT_TEST || environment == WalletConstants.ENVIRONMENT_PRODUCTION

        override fun setAmount(amount: Amount): Builder {
            if (!isSupported(amount.currency) || amount.value < 0) {
                throw CheckoutException("Currency is not valid.")
            }
            this.amount = amount
            return this
        }

        fun setMerchantInfo(merchantInfo: MerchantInfo?): Builder {
            this.merchantInfo = merchantInfo
            return this
        }

        fun setCountryCode(countryCode: String?): Builder {
            this.countryCode = countryCode
            return this
        }

        fun setAllowedAuthMethods(allowedAuthMethods: List<String>?): Builder {
            this.allowedAuthMethods = allowedAuthMethods
            return this
        }

        fun setAllowedCardNetworks(allowedCardNetworks: List<String>?): Builder {
            this.allowedCardNetworks = allowedCardNetworks
            return this
        }

        fun setAllowPrepaidCards(isAllowPrepaidCards: Boolean): Builder {
            this.isAllowPrepaidCards = isAllowPrepaidCards
            return this
        }

        fun setEmailRequired(isEmailRequired: Boolean): Builder {
            this.isEmailRequired = isEmailRequired
            return this
        }

        fun setExistingPaymentMethodRequired(isExistingPaymentMethodRequired: Boolean): Builder {
            this.isExistingPaymentMethodRequired = isExistingPaymentMethodRequired
            return this
        }

        fun setShippingAddressRequired(isShippingAddressRequired: Boolean): Builder {
            this.isShippingAddressRequired = isShippingAddressRequired
            return this
        }

        fun setShippingAddressParameters(shippingAddressParameters: ShippingAddressParameters?): Builder {
            this.shippingAddressParameters = shippingAddressParameters
            return this
        }

        fun setBillingAddressRequired(isBillingAddressRequired: Boolean): Builder {
            this.isBillingAddressRequired = isBillingAddressRequired
            return this
        }

        fun setBillingAddressParameters(billingAddressParameters: BillingAddressParameters?): Builder {
            this.billingAddressParameters = billingAddressParameters
            return this
        }
    }
}
