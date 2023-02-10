package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.payments.Amount

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ButtonComponentParamsMapper {

    fun mapToParams(
        configuration: Configuration,
        overrideComponentParams: ComponentParams? = null,
    ): ButtonComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
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
}
