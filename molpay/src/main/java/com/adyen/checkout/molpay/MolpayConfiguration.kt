/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */
package com.adyen.checkout.molpay

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [MolpayComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class MolpayConfiguration private constructor(
    override val shopperLocale: Locale,
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
     * Builder to create a [MolpayConfiguration].
     */
    class Builder : IssuerListBuilder<MolpayConfiguration, Builder> {

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

        override fun buildInternal(): MolpayConfiguration {
            return MolpayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                viewType = viewType,
                isSubmitButtonVisible = isSubmitButtonVisible,
                hideIssuerLogos = hideIssuerLogos,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.molpay(
    configuration: @CheckoutConfigurationMarker MolpayConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = MolpayConfiguration.Builder(shopperLocale, environment, clientKey)
        .apply {
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()

    MolpayComponent.PAYMENT_METHOD_TYPES.forEach { key ->
        addConfiguration(key, config)
    }

    return this
}

fun CheckoutConfiguration.getMolpayConfiguration(): MolpayConfiguration? {
    return MolpayComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key ->
        getConfiguration(key)
    }
}

internal fun MolpayConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        MolpayComponent.PAYMENT_METHOD_TYPES.forEach { key ->
            addConfiguration(key, this@toCheckoutConfiguration)
        }

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
