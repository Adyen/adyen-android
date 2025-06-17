/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.old.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [CashAppPayComponent].
 */
@Parcelize
class CashAppPayConfiguration
@Suppress("LongParameterList")
private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val genericActionConfiguration: GenericActionConfiguration,
    val cashAppPayEnvironment: CashAppPayEnvironment?,
    val returnUrl: String?,
    val showStorePaymentField: Boolean?,
    val storePaymentMethod: Boolean?,
) : Configuration, ButtonConfiguration {

    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<CashAppPayConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null
        private var cashAppPayEnvironment: CashAppPayEnvironment? = null
        private var returnUrl: String? = null
        private var showStorePaymentField: Boolean? = null
        private var storePaymentMethod: Boolean? = null

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
         * @param context A Context
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
         * Builder with parameters for a [CashAppPayConfiguration].
         *
         * @param shopperLocale The [Locale] of the shopper.
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey,
        )

        /**
         * Sets the environment to be used by Cash App Pay.
         *
         * If not set, it will match the Adyen environment.
         *
         * @param cashAppPayEnvironment The Cash App Pay environment.
         */
        fun setCashAppPayEnvironment(cashAppPayEnvironment: CashAppPayEnvironment): Builder {
            this.cashAppPayEnvironment = cashAppPayEnvironment
            return this
        }

        /**
         *
         * Sets the required return URL that Cash App Pay will redirect to at the end of the transaction.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param returnUrl The Cash App Pay environment.
         */
        fun setReturnUrl(returnUrl: String): Builder {
            this.returnUrl = returnUrl
            return this
        }

        /**
         * Set if the option to store the shopper's account for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param showStorePaymentField [Boolean]
         * @return [CashAppPayConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            this.showStorePaymentField = showStorePaymentField
            return this
        }

        /**
         * Set if the shopper's account should be stored, when the store payment method switch is not presented to the
         * shopper.
         *
         * Only applicable if [showStorePaymentField] is false.
         *
         * Default is false.
         *
         * @param storePaymentMethod [Boolean]
         * @return [CashAppPayConfiguration.Builder]
         */
        fun setStorePaymentMethod(storePaymentMethod: Boolean): Builder {
            this.storePaymentMethod = storePaymentMethod
            return this
        }

        /**
         * Sets if submit button will be visible or not.
         *
         * Default is true.
         *
         * @param isSubmitButtonVisible If submit button should be visible or not.
         */
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal() = CashAppPayConfiguration(
            isSubmitButtonVisible = isSubmitButtonVisible,
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsConfiguration = analyticsConfiguration,
            amount = amount,
            genericActionConfiguration = genericActionConfigurationBuilder.build(),
            cashAppPayEnvironment = cashAppPayEnvironment,
            returnUrl = returnUrl,
            showStorePaymentField = showStorePaymentField,
            storePaymentMethod = storePaymentMethod,
        )
    }
}

fun CheckoutConfiguration.cashAppPay(
    configuration: @CheckoutConfigurationMarker CashAppPayConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = CashAppPayConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.CASH_APP_PAY, config)
    return this
}

internal fun CheckoutConfiguration.getCashAppPayConfiguration(): CashAppPayConfiguration? {
    return getConfiguration(PaymentMethodTypes.CASH_APP_PAY)
}

internal fun CashAppPayConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(PaymentMethodTypes.CASH_APP_PAY, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
