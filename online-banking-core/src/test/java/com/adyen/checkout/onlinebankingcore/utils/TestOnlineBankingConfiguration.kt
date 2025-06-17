/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/11/2022.
 */

package com.adyen.checkout.onlinebankingcore.utils

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.old.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal class TestOnlineBankingConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
) : Configuration,
    ButtonConfiguration {

    class Builder(
        shopperLocale: Locale?,
        environment: Environment,
        clientKey: String
    ) : BaseConfigurationBuilder<TestOnlineBankingConfiguration, Builder>(shopperLocale, environment, clientKey),
        ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null

        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal(): TestOnlineBankingConfiguration {
            return TestOnlineBankingConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
            )
        }
    }
}
