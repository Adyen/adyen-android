/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik

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
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [BlikConfiguration].
 */
@Parcelize
@Suppress("LongParameterList")
class BlikConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [BlikConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<BlikConfiguration, Builder>,
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

        override fun buildInternal(): BlikConfiguration {
            return BlikConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.blik(
    configuration: @CheckoutConfigurationMarker BlikConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = BlikConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.BLIK, config)
    return this
}

internal fun CheckoutConfiguration.getBlikConfiguration(): BlikConfiguration? {
    return getConfiguration(PaymentMethodTypes.BLIK)
}

internal fun BlikConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(PaymentMethodTypes.BLIK, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
