/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto

import android.content.Context
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.action.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [BoletoComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class BoletoConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    override val isSubmitButtonVisible: Boolean?,
    val genericActionConfiguration: GenericActionConfiguration,
    val isEmailVisible: Boolean?
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [BoletoConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<BoletoConfiguration, Builder>,
        ButtonConfigurationBuilder {
        private var isSubmitButtonVisible: Boolean? = null
        private var isEmailVisible: Boolean? = null

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
         * Builder with parameters for a [BoletoConfiguration].
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
         * Sets the visibility of the "send email copy"-switch and email input field.
         *
         * Default value is false
         * @param isEmailVisible
         */
        fun setEmailVisibility(isEmailVisible: Boolean): Builder {
            this.isEmailVisible = isEmailVisible
            return this
        }

        override fun buildInternal() = BoletoConfiguration(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible,
            genericActionConfiguration = genericActionConfigurationBuilder.build(),
            isEmailVisible = isEmailVisible
        )
    }
}
