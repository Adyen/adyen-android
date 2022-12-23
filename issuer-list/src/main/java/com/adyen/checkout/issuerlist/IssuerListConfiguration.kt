/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import android.content.Context
import com.adyen.checkout.action.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.base.ButtonConfiguration
import com.adyen.checkout.components.base.ButtonConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

abstract class IssuerListConfiguration : Configuration, ButtonConfiguration {

    abstract val viewType: IssuerListViewType?
    abstract val hideIssuerLogos: Boolean?

    abstract class IssuerListBuilder<
        IssuerListConfigurationT : IssuerListConfiguration,
        IssuerListBuilderT : IssuerListBuilder<IssuerListConfigurationT, IssuerListBuilderT>
        > :
        ActionHandlingPaymentMethodConfigurationBuilder<IssuerListConfigurationT, IssuerListBuilderT>,
        ButtonConfigurationBuilder {

        protected open var viewType: IssuerListViewType? = null
        protected open var hideIssuerLogos: Boolean? = null
        protected open var isSubmitButtonVisible: Boolean? = null

        protected constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
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
        fun setViewType(viewType: IssuerListViewType): IssuerListBuilderT {
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
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): IssuerListBuilderT {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            @Suppress("UNCHECKED_CAST")
            return this as IssuerListBuilderT
        }
    }
}
