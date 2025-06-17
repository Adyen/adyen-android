/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.old.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class TestConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?
) : Configuration {

    class Builder(shopperLocale: Locale?, environment: Environment, clientKey: String) :
        BaseConfigurationBuilder<TestConfiguration, Builder>(shopperLocale, environment, clientKey) {

        override fun buildInternal(): TestConfiguration {
            return TestConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
            )
        }
    }
}
