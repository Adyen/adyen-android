package com.adyen.checkout.components.core

import android.content.Context
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class ButtonTestConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount,
    override val isSubmitButtonVisible: Boolean?,
) : Configuration, ButtonConfiguration {

    class Builder : BaseConfigurationBuilder<ButtonTestConfiguration, Builder>, ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null

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

        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): ButtonConfigurationBuilder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        override fun buildInternal(): ButtonTestConfiguration {
            return ButtonTestConfiguration(
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
