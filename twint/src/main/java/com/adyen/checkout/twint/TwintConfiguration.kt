/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint

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
 * Configuration class for the [TwintComponent].
 */
@Parcelize
class TwintConfiguration
@Suppress("LongParameterList")
private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val genericActionConfiguration: GenericActionConfiguration,
    val showStorePaymentField: Boolean?,
    val storePaymentMethod: Boolean?,
) : Configuration, ButtonConfiguration {

    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<TwintConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null
        private var showStorePaymentField: Boolean? = null
        private var storePaymentMethod: Boolean? = null

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
         * Builder with parameters for a [TwintConfiguration].
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
         * Set if the option to store the shopper's account for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param showStorePaymentField [Boolean]
         * @return [TwintConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            this.showStorePaymentField = showStorePaymentField
            return this
        }

        /**
         * Set if the shopper's account should be stored, when the store payment method switch is not presented to the
         * shopper.
         *
         * Only applicable if [showStorePaymentField] is false.
         *
         * Default is false.
         *
         * @param storePaymentMethod [Boolean]
         * @return [TwintConfiguration.Builder]
         */
        fun setStorePaymentMethod(storePaymentMethod: Boolean): Builder {
            this.storePaymentMethod = storePaymentMethod
            return this
        }

        /**
         * Sets if submit button will be visible or not.
         *
         * Default is true.
         *
         * @param isSubmitButtonVisible If submit button should be visible or not.
         */
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal() = TwintConfiguration(
            isSubmitButtonVisible = isSubmitButtonVisible,
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsConfiguration = analyticsConfiguration,
            amount = amount,
            genericActionConfiguration = genericActionConfigurationBuilder.build(),
            showStorePaymentField = showStorePaymentField,
            storePaymentMethod = storePaymentMethod,
        )
    }
}

fun CheckoutConfiguration.twint(
    configuration: @CheckoutConfigurationMarker TwintConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = TwintConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.TWINT, config)
    return this
}

internal fun CheckoutConfiguration.getTwintConfiguration(): TwintConfiguration? {
    return getConfiguration(PaymentMethodTypes.TWINT)
}

internal fun TwintConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(PaymentMethodTypes.TWINT, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
