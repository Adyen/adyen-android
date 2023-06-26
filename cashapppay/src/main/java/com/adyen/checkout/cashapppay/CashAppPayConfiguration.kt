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
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [CashAppPayComponent].
 */
@Parcelize
class CashAppPayConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    val genericActionConfiguration: GenericActionConfiguration,
    val cashAppPayEnvironment: CashAppPayEnvironment?,
    val returnUrl: String?,
) : Configuration {

    class Builder : ActionHandlingPaymentMethodConfigurationBuilder<CashAppPayConfiguration, Builder> {

        private var cashAppPayEnvironment: CashAppPayEnvironment? = null
        private var returnUrl: String? = null

        /**
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A Context
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
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
            clientKey
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
         * @param returnUrl The Cash App Pay environment.
         */
        fun setReturnUrl(returnUrl: String): Builder {
            this.returnUrl = returnUrl
            return this
        }

        override fun buildInternal() = CashAppPayConfiguration(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled,
            amount = amount,
            genericActionConfiguration = genericActionConfigurationBuilder.build(),
            cashAppPayEnvironment = cashAppPayEnvironment,
            returnUrl = returnUrl,
        )
    }
}
