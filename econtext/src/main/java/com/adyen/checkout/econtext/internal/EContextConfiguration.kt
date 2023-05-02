/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.econtext.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.action.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import java.util.Locale

abstract class EContextConfiguration : Configuration, ButtonConfiguration {

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    abstract val genericActionConfiguration: GenericActionConfiguration

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
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A context
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
