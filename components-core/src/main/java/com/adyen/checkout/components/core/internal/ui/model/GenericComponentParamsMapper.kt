/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GenericComponentParamsMapper(
    private val isCreatedByDropIn: Boolean,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): GenericComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun Configuration.mapToParamsInternal(): GenericComponentParams {
        return GenericComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount
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
