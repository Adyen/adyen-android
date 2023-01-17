/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.econtext

import android.content.Context
import com.adyen.checkout.action.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.base.ButtonConfiguration
import com.adyen.checkout.components.base.ButtonConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

@Suppress("UnnecessaryAbstractClass")
abstract class EContextConfiguration : Configuration, ButtonConfiguration {

    /**
     * Builder to create an [EContextConfiguration].
     */
    abstract class Builder<
        EContextConfigurationT : EContextConfiguration,
        BuilderT : Builder<EContextConfigurationT, BuilderT>> :
        ActionHandlingPaymentMethodConfigurationBuilder<EContextConfigurationT, BuilderT>,
        ButtonConfigurationBuilder {

        protected var isSubmitButtonVisible: Boolean? = null

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
         * Sets if submit button will be visible or not.
         *
         * Default is True.
         *
         * @param isSubmitButtonVisible Is submit button should be visible or not.
         */
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): BuilderT {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            @Suppress("UNCHECKED_CAST")
            return this as BuilderT
        }
    }
}
