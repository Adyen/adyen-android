/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.adyen3ds2

import android.content.Context
import android.content.IntentFilter
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import com.adyen.threeds2.customization.UiCustomization
import com.adyen.threeds2.internal.ui.activity.ChallengeActivity
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [Adyen3DS2Component].
 */
@Suppress("LongParameterList")
@Parcelize
@Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
class Adyen3DS2Configuration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    val uiCustomization: UiCustomization?,
    val threeDSRequestorAppURL: String?,
) : Configuration {

    /**
     * Builder to create an [Adyen3DS2Configuration].
     */
    @Deprecated("Configuration builders are deprecated, use CheckoutConfiguration instead.")
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration, Builder> {

        var uiCustomization: UiCustomization? = null

        var threeDSRequestorAppURL: String? = null

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
         * Set a [UiCustomization] object to be passed to the 3DS2 SDK for customizing the challenge screen.
         *
         * @param uiCustomization The customization object.
         */
        @Deprecated("Use property access syntax instead.")
        fun setUiCustomization(uiCustomization: UiCustomization?): Builder {
            this.uiCustomization = uiCustomization
            return this
        }

        /**
         * Sets the 3DS Requestor App URL. This is used to call your app after an out-of-band (OOB)
         * authentication occurs.
         *
         * Make sure to also override [ChallengeActivity]'s [IntentFilter] with your own URL like
         * [this](https://docs.adyen.com/online-payments/classic-integrations/api-integration-ecommerce/3d-secure/native-3ds2/android-sdk-integration#handling-android-app-links)
         * when using this method.
         */
        @Suppress("MaxLineLength")
        @Deprecated("Use property access syntax instead.")
        fun setThreeDSRequestorAppURL(threeDSRequestorAppURL: String): Builder {
            this.threeDSRequestorAppURL = threeDSRequestorAppURL
            return this
        }

        override fun buildInternal(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                uiCustomization = uiCustomization,
                threeDSRequestorAppURL = threeDSRequestorAppURL,
            )
        }
    }
}

fun CheckoutConfiguration.adyen3DS2(
    configuration: @CheckoutConfigurationMarker Adyen3DS2Configuration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = Adyen3DS2Configuration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addActionConfiguration(config)
    return this
}

internal fun CheckoutConfiguration.getAdyen3DS2Configuration(): Adyen3DS2Configuration? {
    return getActionConfiguration(Adyen3DS2Configuration::class.java)
}

internal fun Adyen3DS2Configuration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addActionConfiguration(this@toCheckoutConfiguration)
    }
}
