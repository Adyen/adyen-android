/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.action.core

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionConfiguration.Builder
import com.adyen.checkout.action.core.internal.ActionHandlingConfigurationBuilder
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale
import kotlin.collections.set

/**
 * This is the base configuration for [GenericActionComponent]. You need to use the [Builder] to instantiate this
 * class.
 * There you will find specific methods to add configurations for each specific [ActionComponent], to be able to
 * customize their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
@Parcelize
class GenericActionConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    private val availableActionConfigs: HashMap<Class<*>, Configuration>,
) : Configuration {

    internal inline fun <reified T : Configuration> getConfigurationForAction(): T? {
        val actionClass = T::class.java
        if (availableActionConfigs.containsKey(actionClass)) {
            return availableActionConfigs[actionClass] as T
        }
        return null
    }

    /**
     * Builder for creating a [GenericActionConfiguration] where you can set specific Configurations for each action
     * component.
     */
    @Suppress("unused")
    class Builder :
        BaseConfigurationBuilder<GenericActionConfiguration, Builder>,
        ActionHandlingConfigurationBuilder<Builder> {

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        val availableActionConfigs = HashMap<Class<*>, Configuration>()

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
         * Add configuration for 3DS2 action.
         */
        override fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Await action.
         */
        override fun addAwaitActionConfiguration(configuration: AwaitConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for QR code action.
         */
        override fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Redirect action.
         */
        override fun addRedirectActionConfiguration(configuration: RedirectConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for WeChat Pay action.
         */
        override fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Voucher action.
         */
        override fun addVoucherActionConfiguration(configuration: VoucherConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        override fun buildInternal(): GenericActionConfiguration {
            return GenericActionConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                amount = amount,
                availableActionConfigs = availableActionConfigs,
            )
        }
    }
}
