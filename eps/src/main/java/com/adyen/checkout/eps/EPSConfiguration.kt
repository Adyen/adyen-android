/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.eps

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [EPSComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class EPSConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val viewType: IssuerListViewType?,
    override val isSubmitButtonVisible: Boolean?,
    override val hideIssuerLogos: Boolean?,
    override val genericActionConfiguration: GenericActionConfiguration,
) : IssuerListConfiguration() {

    /**
     * Builder to create an [EPSConfiguration].
     */
    class Builder : IssuerListBuilder<EPSConfiguration, Builder> {

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
         * Sets whether the logos should be shown next to the issuers name.
         *
         * Default is true.
         *
         * @param hideIssuerLogos if issuer logos should be hidden or not.
         */
        override fun setHideIssuerLogos(hideIssuerLogos: Boolean): Builder {
            return super.setHideIssuerLogos(hideIssuerLogos)
        }

        /**
         * Initialize a configuration builder with the required fields.
         *
         * @param shopperLocale The [Locale] of the shopper.
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        public override fun buildInternal(): EPSConfiguration {
            return EPSConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                viewType = viewType,
                isSubmitButtonVisible = isSubmitButtonVisible,
                hideIssuerLogos = hideIssuerLogos,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}