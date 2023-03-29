/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.model

import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams

internal class Adyen3DS2ComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        adyen3DS2Configuration: Adyen3DS2Configuration,
        sessionParams: SessionParams?,
        defaultThreeDSRequestorAppURL: String,
    ): Adyen3DS2ComponentParams {
        return adyen3DS2Configuration
            .mapToParamsInternal(defaultThreeDSRequestorAppURL)
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun Adyen3DS2Configuration.mapToParamsInternal(
        defaultThreeDSRequestorAppURL: String,
    ): Adyen3DS2ComponentParams {
        return Adyen3DS2ComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            uiCustomization = uiCustomization,
            threeDSRequestorAppURL = threeDSRequestorAppURL ?: defaultThreeDSRequestorAppURL,
        )
    }

    private fun Adyen3DS2ComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): Adyen3DS2ComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun Adyen3DS2ComponentParams.override(
        sessionParams: SessionParams? = null
    ): Adyen3DS2ComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
