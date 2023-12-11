/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/11/2023.
 */

package com.adyen.checkout.giftcard.internal.ui.model

import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.GiftCardConfiguration

internal class GiftCardComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: GiftCardConfiguration,
        sessionParams: SessionParams?,
    ): GiftCardComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun GiftCardConfiguration.mapToParamsInternal(): GiftCardComponentParams {
        return GiftCardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = (this as? ButtonConfiguration)?.isSubmitButtonVisible ?: true,
            isPinRequired = isPinRequired ?: true,
        )
    }

    private fun GiftCardComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): GiftCardComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            analyticsParams = overrideComponentParams.analyticsParams,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount
        )
    }

    private fun GiftCardComponentParams.override(
        sessionParams: SessionParams? = null
    ): GiftCardComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
