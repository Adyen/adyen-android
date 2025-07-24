/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

@file:Suppress("unused", "DEPRECATION")

package com.adyen.checkout.googlepay

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
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
@Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
class GooglePayConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val merchantAccount: String?,
    val googlePayEnvironment: Int?,
    val totalPriceStatus: String?,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>?,
    val allowedCardNetworks: List<String>?,
    val isAllowPrepaidCards: Boolean?,
    val isAllowCreditCards: Boolean?,
    val isAssuranceDetailsRequired: Boolean?,
    val isEmailRequired: Boolean?,
    val isExistingPaymentMethodRequired: Boolean?,
    val isShippingAddressRequired: Boolean?,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean?,
    val billingAddressParameters: BillingAddressParameters?,
    val checkoutOption: String?,
    val googlePayButtonStyling: GooglePayButtonStyling?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [GooglePayConfiguration].
     */
    @Suppress("TooManyFunctions")
    @Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<GooglePayConfiguration, Builder>,
        ButtonConfigurationBuilder {
        var merchantAccount: String? = null
        var googlePayEnvironment: Int? = null
            set(value) {
                if (value != null && !isGooglePayEnvironmentValid(value)) {
                    throw CheckoutException(
                        "Invalid value for Google Environment. Use either WalletConstants.ENVIRONMENT_TEST or" +
                            " WalletConstants.ENVIRONMENT_PRODUCTION",
                    )
                }
                field = value
            }
        var merchantInfo: MerchantInfo? = null
        var countryCode: String? = null
        var allowedAuthMethods: List<String>? = null
        var allowedCardNetworks: List<String>? = null
        var isAllowPrepaidCards: Boolean? = null
        var isAllowCreditCards: Boolean? = null
        var isAssuranceDetailsRequired: Boolean? = null
        var isEmailRequired: Boolean? = null
        var isExistingPaymentMethodRequired: Boolean? = null
        var isShippingAddressRequired: Boolean? = null
        var shippingAddressParameters: ShippingAddressParameters? = null
        var isBillingAddressRequired: Boolean? = null
        var billingAddressParameters: BillingAddressParameters? = null
        var totalPriceStatus: String? = null
        var checkoutOption: String? = null
        var googlePayButtonStyling: GooglePayButtonStyling? = null

        @Deprecated("Configure this in CheckoutConfiguration instead.")
        var isSubmitButtonVisible: Boolean? = null

        /**
         * Initialize a configuration builder with the required fields.
         *
         * The shopper locale will match the value passed to the API with the sessions flow, or the primary user locale
         * on the device otherwise. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(environment: Environment, clientKey: String) : super(
            environment,
            clientKey,
        )

        /**
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A context
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        @Deprecated("You can omit the context parameter")
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey,
        )

        /**
         * Initialize a configuration builder with the required fields and a shopper locale.
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setGooglePayEnvironment(googlePayEnvironment: Int): Builder {
            this.googlePayEnvironment = googlePayEnvironment
            return this
        }

        private fun isGooglePayEnvironmentValid(environment: Int?): Boolean =
            environment == WalletConstants.ENVIRONMENT_TEST || environment == WalletConstants.ENVIRONMENT_PRODUCTION

        /**
         * Sets the information about the merchant requesting the payment.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
         * for more details.
         *
         */
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setCountryCode(countryCode: String?): Builder {
            this.countryCode = countryCode
            return this
        }

        /**
         * Sets the supported authentication methods. Check [AllowedAuthMethods] for all the possible values.
         *
         * Default is ["PAN_ONLY", "CRYPTOGRAM_3DS"].
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        @Deprecated("Use property access syntax instead.")
        fun setAllowedAuthMethods(allowedAuthMethods: List<String>?): Builder {
            this.allowedAuthMethods = allowedAuthMethods
            return this
        }

        /**
         * Sets the allowed card networks. The allowed networks are automatically configured based on your account
         * settings, but you can override them here. Check [AllowedCardNetworks] for all the possible values.
         *
         * Default is ["AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA"].
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setAllowPrepaidCards(isAllowPrepaidCards: Boolean): Builder {
            this.isAllowPrepaidCards = isAllowPrepaidCards
            return this
        }

        /**
         * Set if you support credit cards.
         *
         * Default is true for the specified card networks.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        @Deprecated("Use property access syntax instead.")
        fun setAllowCreditCards(isAllowCreditCards: Boolean): Builder {
            this.isAllowCreditCards = isAllowCreditCards
            return this
        }

        /**
         * Set to true to request assurance details.
         *
         * Default is false.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
         * for more details.
         *
         */
        @Deprecated("Use property access syntax instead.")
        fun setAssuranceDetailsRequired(isAssuranceDetailsRequired: Boolean): Builder {
            this.isAssuranceDetailsRequired = isAssuranceDetailsRequired
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setTotalPriceStatus(totalPriceStatus: String): Builder {
            this.totalPriceStatus = totalPriceStatus
            return this
        }

        /**
         * Sets the checkout option. This affects the submit button text displayed in the Google Pay sheet.
         *
         * Check the
         * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo)
         * for more details.
         */
        @Deprecated("Use property access syntax instead.")
        fun setCheckoutOption(checkoutOption: String): Builder {
            this.checkoutOption = checkoutOption
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
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param amount Amount of the transaction.
         */
        @Deprecated("Configure this in CheckoutConfiguration instead.")
        override fun setAmount(amount: Amount): Builder {
            return super.setAmount(amount)
        }

        /**
         * Set a [GooglePayButtonStyling] object for customization of the Google Pay button.
         *
         * @param googlePayButtonStyling The customization object.
         */
        @Deprecated("Use property access syntax instead.")
        fun setGooglePayButtonStyling(googlePayButtonStyling: GooglePayButtonStyling): Builder {
            this.googlePayButtonStyling = googlePayButtonStyling
            return this
        }

        /**
         * Sets if submit button will be visible or not.
         *
         * Default is false.
         *
         * @param isSubmitButtonVisible If submit button should be visible or not.
         */
        @Deprecated("Configure this in CheckoutConfiguration instead.")
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal(): GooglePayConfiguration {
            return GooglePayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
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
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.googlePay(
    configuration: @CheckoutConfigurationMarker GooglePayConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = GooglePayConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
            isSubmitButtonVisible?.let { setSubmitButtonVisible(it) }
        }
        .apply(configuration)
        .build()

    GooglePayComponent.PAYMENT_METHOD_TYPES.forEach { key ->
        addConfiguration(key, config)
    }

    return this
}

internal fun CheckoutConfiguration.getGooglePayConfiguration(): GooglePayConfiguration? {
    return GooglePayComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key -> getConfiguration(key) }
}

internal fun GooglePayConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ) {
        GooglePayComponent.PAYMENT_METHOD_TYPES.forEach { key ->
            addConfiguration(key, this@toCheckoutConfiguration)
        }

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
