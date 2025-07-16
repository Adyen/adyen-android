/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/1/2023.
 */

@file:Suppress("DEPRECATION")

package com.adyen.checkout.seveneleven

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import com.adyen.checkout.econtext.internal.EContextConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [SevenElevenComponent].
 */
@Suppress("LongParameterList")
@Parcelize
@Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
class SevenElevenConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    override val genericActionConfiguration: GenericActionConfiguration,
) : EContextConfiguration() {

    /**
     * Builder to create a [SevenElevenConfiguration].
     */
    @Deprecated("Configuration builders are deprecated, use CheckoutConfiguration instead.")
    class Builder : EContextConfiguration.Builder<SevenElevenConfiguration, Builder> {

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

        override fun buildInternal(): SevenElevenConfiguration {
            return SevenElevenConfiguration(
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

fun CheckoutConfiguration.sevenEleven(
    configuration: @CheckoutConfigurationMarker SevenElevenConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = SevenElevenConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
            isSubmitButtonVisible?.let { setSubmitButtonVisible(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN, config)
    return this
}

internal fun CheckoutConfiguration.getSevenElevenConfiguration(): SevenElevenConfiguration? {
    return getConfiguration(PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN)
}

internal fun SevenElevenConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ) {
        addConfiguration(PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
