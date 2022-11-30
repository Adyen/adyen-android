/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/6/2019.
 */
package com.adyen.checkout.ideal

import android.content.Context
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import com.adyen.checkout.issuerlist.IssuerListViewType
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class IdealConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val viewType: IssuerListViewType?,
    override val hideIssuerLogos: Boolean?,
) : IssuerListConfiguration() {

    /**
     * Builder to create a [IdealConfiguration].
     */
    class Builder : IssuerListBuilder<IdealConfiguration, Builder> {

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

        override fun buildInternal(): IdealConfiguration {
            return IdealConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
            )
        }
    }
}
