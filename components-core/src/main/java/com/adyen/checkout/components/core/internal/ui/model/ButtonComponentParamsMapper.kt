package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ButtonComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: Configuration,
        sessionParams: SessionParams?,
    ): ButtonComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun Configuration.mapToParamsInternal(): ButtonComponentParams {
        return ButtonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = (this as? ButtonConfiguration)?.isSubmitButtonVisible ?: true
        )
    }

    private fun ButtonComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): ButtonComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount
        )
    }

    private fun ButtonComponentParams.override(
        sessionParams: SessionParams? = null
    ): ButtonComponentParams {
        if (sessionParams == null) return this
        return copy()
    }
}
