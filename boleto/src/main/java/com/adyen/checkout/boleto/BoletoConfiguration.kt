/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

@file:Suppress("DEPRECATION")

package com.adyen.checkout.boleto

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [BoletoComponent].
 */
@Parcelize
@Suppress("LongParameterList")
@Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
class BoletoConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val genericActionConfiguration: GenericActionConfiguration,
    val isEmailVisible: Boolean?
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [BoletoConfiguration].
     */
    @Deprecated("Configuration builders are deprecated, use CheckoutConfiguration instead.")
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<BoletoConfiguration, Builder>,
        ButtonConfigurationBuilder {

        @Deprecated("Configure this in CheckoutConfiguration instead.")
        var isSubmitButtonVisible: Boolean? = null
        var isEmailVisible: Boolean? = null

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
         * Builder with parameters for a [BoletoConfiguration].
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
        @Deprecated("Configure this in CheckoutConfiguration instead.")
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        /**
         * Sets the visibility of the "send email copy"-switch and email input field.
         *
         * Default value is false
         * @param isEmailVisible
         */
        @Deprecated("Use property access syntax instead.")
        fun setEmailVisibility(isEmailVisible: Boolean): Builder {
            this.isEmailVisible = isEmailVisible
            return this
        }

        override fun buildInternal() = BoletoConfiguration(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsConfiguration = analyticsConfiguration,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible,
            genericActionConfiguration = genericActionConfigurationBuilder.build(),
            isEmailVisible = isEmailVisible,
        )
    }
}

fun CheckoutConfiguration.boleto(
    configuration: @CheckoutConfigurationMarker BoletoConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = BoletoConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
            isSubmitButtonVisible?.let { setSubmitButtonVisible(it) }
        }
        .apply(configuration)
        .build()

    BoletoComponent.PAYMENT_METHOD_TYPES.forEach { key ->
        addConfiguration(key, config)
    }

    return this
}

internal fun CheckoutConfiguration.getBoletoConfiguration(): BoletoConfiguration? {
    return BoletoComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key ->
        getConfiguration(key)
    }
}

internal fun BoletoConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ) {
        BoletoComponent.PAYMENT_METHOD_TYPES.forEach { key ->
            addConfiguration(key, this@toCheckoutConfiguration)
        }

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
