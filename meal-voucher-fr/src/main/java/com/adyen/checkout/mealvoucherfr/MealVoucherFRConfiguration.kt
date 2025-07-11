/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr

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
 * Configuration class for the [MealVoucherFRComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class MealVoucherFRConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val isSecurityCodeRequired: Boolean?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [MealVoucherFRConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<MealVoucherFRConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isSecurityCodeRequired: Boolean? = null
        private var isSubmitButtonVisible: Boolean? = null

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
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        /**
         * Set if the Security code field should be hidden from the Component and not requested to the shopper.
         * Note that this might have implications for the transaction.
         *
         * Default is true.
         *
         * @param isSecurityCodeRequired If Security code should be hidden or not.
         */
        fun setSecurityCodeRequired(isSecurityCodeRequired: Boolean): Builder {
            this.isSecurityCodeRequired = isSecurityCodeRequired
            return this
        }

        override fun buildInternal(): MealVoucherFRConfiguration {
            return MealVoucherFRConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
                isSecurityCodeRequired = isSecurityCodeRequired,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.mealVoucherFR(
    configuration: @CheckoutConfigurationMarker MealVoucherFRConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = MealVoucherFRConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
            isSubmitButtonVisible?.let { setSubmitButtonVisible(it) }
        }
        .apply(configuration)
        .build()

    MealVoucherFRComponent.PAYMENT_METHOD_TYPES.forEach { key ->
        addConfiguration(key, config)
    }

    return this
}

internal fun CheckoutConfiguration.getMealVoucherFRConfiguration(): MealVoucherFRConfiguration? {
    return MealVoucherFRComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key ->
        getConfiguration(key)
    }
}

internal fun MealVoucherFRConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ) {
        MealVoucherFRComponent.PAYMENT_METHOD_TYPES.forEach { key ->
            addConfiguration(key, this@toCheckoutConfiguration)
        }

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
