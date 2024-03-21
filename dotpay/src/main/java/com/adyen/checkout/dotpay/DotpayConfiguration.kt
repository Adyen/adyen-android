/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.dotpay

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Amount
import com.adyen.checkout.core.AnalyticsConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [DotpayComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class DotpayConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val viewType: IssuerListViewType?,
    override val isSubmitButtonVisible: Boolean?,
    override val hideIssuerLogos: Boolean?,
    override val genericActionConfiguration: GenericActionConfiguration,
) : IssuerListConfiguration() {

    /**
     * Builder to create a [DotpayConfiguration].
     */
    class Builder : IssuerListBuilder<DotpayConfiguration, Builder> {

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

        override fun buildInternal(): DotpayConfiguration {
            return DotpayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
                isSubmitButtonVisible = isSubmitButtonVisible,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.dotpay(
    configuration: @CheckoutConfigurationMarker DotpayConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = DotpayConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.DOTPAY, config)
    return this
}

fun CheckoutConfiguration.getDotpayConfiguration(): DotpayConfiguration? {
    return getConfiguration(PaymentMethodTypes.DOTPAY)
}

internal fun DotpayConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(PaymentMethodTypes.DOTPAY, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
