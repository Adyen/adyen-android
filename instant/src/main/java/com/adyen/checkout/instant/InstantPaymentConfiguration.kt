/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
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
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    val actionHandlingMethod: ActionHandlingMethod?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration {

    /**
     * Builder to create an [InstantPaymentConfiguration].
     */
    class Builder : ActionHandlingPaymentMethodConfigurationBuilder<InstantPaymentConfiguration, Builder> {

        private var actionHandlingMethod: ActionHandlingMethod? = null

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
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey,
        )

        /**
         * Sets the method used to handle actions. See [ActionHandlingMethod] for the available options.
         *
         * Default is [ActionHandlingMethod.PREFER_NATIVE].
         */
        fun setActionHandlingMethod(actionHandlingMethod: ActionHandlingMethod): Builder {
            this.actionHandlingMethod = actionHandlingMethod
            return this
        }

        override fun buildInternal(): InstantPaymentConfiguration {
            return InstantPaymentConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                actionHandlingMethod = actionHandlingMethod,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
const val GLOBAL_INSTANT_CONFIG_KEY = "GLOBAL_INSTANT_CONFIG_KEY"

fun CheckoutConfiguration.instantPayment(
    paymentMethod: String = GLOBAL_INSTANT_CONFIG_KEY,
    configuration: @CheckoutConfigurationMarker InstantPaymentConfiguration.Builder.() -> Unit = {},
): CheckoutConfiguration {
    val config = InstantPaymentConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(paymentMethod, config)
    return this
}

internal fun CheckoutConfiguration.getInstantPaymentConfiguration(
    paymentMethod: String = GLOBAL_INSTANT_CONFIG_KEY,
): InstantPaymentConfiguration? {
    return getConfiguration(paymentMethod)
}

internal fun InstantPaymentConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(GLOBAL_INSTANT_CONFIG_KEY, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
