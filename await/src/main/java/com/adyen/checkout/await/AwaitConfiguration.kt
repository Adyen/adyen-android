/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 19/8/2020.
 */
package com.adyen.checkout.await

import android.content.Context
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [AwaitComponent].
 */
@Parcelize
class AwaitConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?
) : Configuration {

    /**
     * Builder to create an [AwaitConfiguration].
     */
    class Builder : BaseConfigurationBuilder<AwaitConfiguration, Builder> {

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

        override fun buildInternal(): AwaitConfiguration {
            return AwaitConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
            )
        }
    }
}
