/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [InstantPaymentComponent].
 */
@Parcelize
class InstantPaymentConfiguration
@Suppress("LongParameterList")
private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    val shouldUseSdk: Boolean?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration {

    /**
     * Builder to create an [InstantPaymentConfiguration].
     */
    class Builder : ActionHandlingPaymentMethodConfigurationBuilder<InstantPaymentConfiguration, Builder> {

        private var shouldUseSdk: Boolean? = null

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
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey
        )

        /**
         * Sets if the payment method will be handled with the corresponding SDK or with a web flow. If there is no SDK
         * available then the payment method will be handled with a web flow.
         *
         * Default is true.
         */
        fun setUseSdk(shouldUseSdk: Boolean): Builder {
            this.shouldUseSdk = shouldUseSdk
            return this
        }

        override fun buildInternal(): InstantPaymentConfiguration {
            return InstantPaymentConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                shouldUseSdk = shouldUseSdk,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}
