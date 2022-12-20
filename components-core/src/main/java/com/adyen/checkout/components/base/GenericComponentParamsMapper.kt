/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
) {

    fun mapToParams(
        configuration: Configuration,
    ): GenericComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
    }

    private fun Configuration.mapToParamsInternal(): GenericComponentParams {
        return GenericComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
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
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount
        )
    }
}
