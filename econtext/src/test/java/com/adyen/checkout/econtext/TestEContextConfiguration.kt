/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/1/2023.
 */

package com.adyen.checkout.econtext

import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.econtext.internal.EContextConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal class TestEContextConfiguration
@Suppress("LongParameterList")
private constructor(
    override val isSubmitButtonVisible: Boolean?,
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val genericActionConfiguration: GenericActionConfiguration
) : EContextConfiguration() {

    class Builder(shopperLocale: Locale?, environment: Environment, clientKey: String) :
        EContextConfiguration.Builder<TestEContextConfiguration, Builder>(environment, clientKey) {

        init {
            shopperLocale?.let { setShopperLocale(it) }
        }

        public override fun buildInternal(): TestEContextConfiguration {
            return TestEContextConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}
