/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.eps

import android.content.Context
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import com.adyen.checkout.issuerlist.IssuerListViewType
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class EPSConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val viewType: IssuerListViewType,
    override val hideIssuerLogos: Boolean,
) : IssuerListConfiguration() {

    /**
     * Builder to create a [EPSConfiguration].
     */
    class Builder : IssuerListBuilder<EPSConfiguration> {

        override var hideIssuerLogos: Boolean = true

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
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: EPSConfiguration) : super(configuration) {
            viewType = configuration.viewType
            hideIssuerLogos = configuration.hideIssuerLogos
        }

        /**
         * Sets whether the logos should be shows next to the issuers name.
         *
         * Default is true.
         *
         * @param hideIssuerLogos if issuer logos should be hidden or not.
         */
        override fun setHideIssuerLogos(hideIssuerLogos: Boolean): Builder {
            return super.setHideIssuerLogos(hideIssuerLogos) as Builder
        }

        public override fun buildInternal(): EPSConfiguration {
            return EPSConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
            )
        }
    }
}
