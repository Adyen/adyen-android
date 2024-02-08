package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ButtonComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        configuration: Configuration?,
        sessionParams: SessionParams?,
    ): ButtonComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal(configuration)
            .override(dropInOverrideParams)
            .override(sessionParams ?: dropInOverrideParams?.sessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(configuration: Configuration?): ButtonComponentParams {
        return ButtonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = (configuration as? ButtonConfiguration)?.isSubmitButtonVisible ?: true,
        )
    }

    private fun ButtonComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): ButtonComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun ButtonComponentParams.override(
        sessionParams: SessionParams? = null
    ): ButtonComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
