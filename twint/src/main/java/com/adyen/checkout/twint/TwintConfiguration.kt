/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class TwintConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration {

    class Builder : ActionHandlingPaymentMethodConfigurationBuilder<TwintConfiguration, Builder> {

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
        @Suppress("DEPRECATION")
        @Deprecated("You can omit the context parameter")
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey,
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

        override fun buildInternal(): TwintConfiguration {
            return TwintConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.twint(
    configuration: @CheckoutConfigurationMarker TwintConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = TwintConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addActionConfiguration(config)
    return this
}

internal fun CheckoutConfiguration.getTwintConfiguration(): TwintConfiguration? {
    return getActionConfiguration(TwintConfiguration::class.java)
}

internal fun TwintConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addActionConfiguration(this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
