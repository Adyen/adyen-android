/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.utils

import android.content.Context
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import com.adyen.checkout.issuerlist.IssuerListViewType
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
class TestIssuerListConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val viewType: IssuerListViewType?,
    override val hideIssuerLogos: Boolean?,
) : IssuerListConfiguration() {

    class Builder : IssuerListBuilder<TestIssuerListConfiguration, Builder> {

        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        constructor(configuration: TestIssuerListConfiguration) : super(configuration) {
            viewType = configuration.viewType
            hideIssuerLogos = configuration.hideIssuerLogos
        }

        public override fun buildInternal(): TestIssuerListConfiguration {
            return TestIssuerListConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
            )
        }
    }
}
