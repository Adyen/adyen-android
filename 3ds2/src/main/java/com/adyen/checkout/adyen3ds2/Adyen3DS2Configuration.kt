/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.adyen3ds2

import android.content.Context
import androidx.annotation.DrawableRes
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class Adyen3DS2Configuration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    @DrawableRes val merchantLogo: Int?
) : Configuration {

    /**
     * Builder to create a [Adyen3DS2Configuration].
     */
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration, Builder> {

        @DrawableRes
        private var merchantLogo: Int? = null

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey
        )

        /**
         * Set the merchant logo that will be shown during Delegated Authentication.
         * If not set or null - default icon will be shown.
         *
         * @param merchantLogo The drawable resource id of the merchant logo
         * @return [Adyen3DS2Configuration.Builder]
         */
        fun setMerchantLogo(@DrawableRes merchantLogo: Int?): Builder {
            this.merchantLogo = merchantLogo
            return this
        }

        override fun buildInternal(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                amount = amount,
                merchantLogo = merchantLogo
            )
        }
    }
}
