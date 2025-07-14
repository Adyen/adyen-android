/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

@file:Suppress("DEPRECATION")

package com.adyen.checkout.issuerlist.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import java.util.Locale

abstract class IssuerListConfiguration : Configuration, ButtonConfiguration {

    abstract val viewType: IssuerListViewType?
    abstract val hideIssuerLogos: Boolean?

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    abstract val genericActionConfiguration: GenericActionConfiguration

    abstract class IssuerListBuilder<
        IssuerListConfigurationT : IssuerListConfiguration,
        IssuerListBuilderT : IssuerListBuilder<IssuerListConfigurationT, IssuerListBuilderT>
        > :
        ActionHandlingPaymentMethodConfigurationBuilder<IssuerListConfigurationT, IssuerListBuilderT>,
        ButtonConfigurationBuilder {

        open var viewType: IssuerListViewType? = null
        open var hideIssuerLogos: Boolean? = null

        @Deprecated("Configure this in CheckoutConfiguration instead.")
        open var isSubmitButtonVisible: Boolean? = null

        protected constructor(environment: Environment, clientKey: String) : super(
            environment,
            clientKey,
        )

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
         * Sets the type of the view to be show with the component.
         *
         * Default is [IssuerListViewType.RECYCLER_VIEW].
         *
         * @param viewType an enum with the view type options.
         */
        @Deprecated("Use property access syntax instead.")
        open fun setViewType(viewType: IssuerListViewType): IssuerListBuilderT {
            this.viewType = viewType
            @Suppress("UNCHECKED_CAST")
            return this as IssuerListBuilderT
        }

        /**
         * Sets whether the logos should be shown next to the issuers name.
         *
         * Default is false.
         *
         * @param hideIssuerLogos if issuer logos should be hidden or not.
         */
        @Deprecated("Use property access syntax instead.")
        open fun setHideIssuerLogos(hideIssuerLogos: Boolean): IssuerListBuilderT {
            this.hideIssuerLogos = hideIssuerLogos
            @Suppress("UNCHECKED_CAST")
            return this as IssuerListBuilderT
        }

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
