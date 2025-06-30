/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import android.content.Context
import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.analytics.AnalyticsConfiguration
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.data.model.Amount
import com.adyen.checkout.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.core.internal.ButtonConfiguration
import com.adyen.checkout.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.core.internal.Configuration
import kotlinx.parcelize.Parcelize
import java.util.Locale

// TODO - Change MBWayComponent to the new name
/**
 * Configuration class for the [MBWayComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class MBWayConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,

    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,

    // TODO - Actions
//    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create an [MBWayConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<MBWayConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null

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
         * Sets if submit button will be visible or not.
         *
         * Default is True.
         *
         * @param isSubmitButtonVisible Is submit button should be visible or not.
         */
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal(): MBWayConfiguration {
            return MBWayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
//                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.mbWay(
    configuration: @CheckoutConfigurationMarker MBWayConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = MBWayConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    // TODO - Add PaymentMethodTypes to core module
    addConfiguration("mbway", config)
    return this
}

internal fun CheckoutConfiguration.getMBWayConfiguration(): MBWayConfiguration? {
    // TODO - Add PaymentMethodTypes to core module
    return getConfiguration("mbway")
}

internal fun MBWayConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        // TODO - Add PaymentMethodTypes to core module
        addConfiguration("mbway", this@toCheckoutConfiguration)

        // TODO - Actions support
//        genericActionConfiguration.getAllConfigurations().forEach {
//            addActionConfiguration(it)
//        }
    }
}
