/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.adyen3ds2

import android.content.Context
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [Adyen3DS2Component].
 */
@Parcelize
class Adyen3DS2Configuration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    val uiCustomization: UiCustomization?,
) : Configuration {

    /**
     * Builder to create an [Adyen3DS2Configuration].
     */
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration, Builder> {

        private var uiCustomization: UiCustomization? = null

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

        /**
         * Set a [UiCustomization] object to be passed to the 3DS2 SDK for customizing the challenge screen.
         *
         * @param uiCustomization The customization object.
         */
        fun setUiCustomization(uiCustomization: UiCustomization?): Builder {
            this.uiCustomization = uiCustomization
            return this
        }

        override fun buildInternal(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                amount = amount,
                uiCustomization = uiCustomization,
            )
        }
    }
}
