/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.utils

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.Environment
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class TestIssuerListConfiguration
@Suppress("LongParameterList")
private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    override val amount: Amount,
    override val viewType: IssuerListViewType?,
    override val isSubmitButtonVisible: Boolean?,
    override val hideIssuerLogos: Boolean?,
    override val genericActionConfiguration: GenericActionConfiguration,
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

        public override fun buildInternal(): TestIssuerListConfiguration {
            return TestIssuerListConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled,
                amount = amount,
                viewType = viewType,
                isSubmitButtonVisible = isSubmitButtonVisible,
                hideIssuerLogos = hideIssuerLogos,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}
