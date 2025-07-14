/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/1/2023.
 */

package com.adyen.checkout.onlinebankingcore.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import java.util.Locale

abstract class OnlineBankingConfiguration : Configuration, ButtonConfiguration {

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    abstract val genericActionConfiguration: GenericActionConfiguration

    abstract class OnlineBankingConfigurationBuilder<
        OnlineBankingConfigurationT : OnlineBankingConfiguration,
        IssuerListBuilderT : OnlineBankingConfigurationBuilder<OnlineBankingConfigurationT, IssuerListBuilderT>
        > :
        ActionHandlingPaymentMethodConfigurationBuilder<OnlineBankingConfigurationT, IssuerListBuilderT>,
        ButtonConfigurationBuilder {

        @Deprecated("Configure this in CheckoutConfiguration instead.")
        open var isSubmitButtonVisible: Boolean? = null

        protected constructor(environment: Environment, clientKey: String) : super(environment, clientKey)

        @Deprecated("You can omit the context parameter")
        protected constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey,
        )

        protected constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Sets if submit button will be visible or not.
         *
         * Default is True.
         *
         * @param isSubmitButtonVisible Is submit button should be visible or not.
         */
        @Deprecated("Configure this in CheckoutConfiguration instead.")
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): IssuerListBuilderT {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            @Suppress("UNCHECKED_CAST")
            return this as IssuerListBuilderT
        }
    }
}
