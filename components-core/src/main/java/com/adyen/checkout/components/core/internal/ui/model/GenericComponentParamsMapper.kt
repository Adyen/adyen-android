/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericComponentParamsMapper(
    // TODO: Replace with just CheckoutConfiguration
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: Configuration,
        sessionParams: SessionParams?,
    ): GenericComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun Configuration.mapToParamsInternal(): GenericComponentParams {
        return GenericComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount
        )
    }

    private fun GenericComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): GenericComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            analyticsParams = overrideComponentParams.analyticsParams,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun GenericComponentParams.override(
        sessionParams: SessionParams? = null
    ): GenericComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
