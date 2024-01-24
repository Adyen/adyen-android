/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/11/2023.
 */

package com.adyen.checkout.giftcard.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.giftcard.getGiftCardConfiguration

internal class GiftCardComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): GiftCardComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(): GiftCardComponentParams {
        val giftCardConfiguration = getGiftCardConfiguration()
        return GiftCardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = giftCardConfiguration?.isSubmitButtonVisible ?: true,
            isPinRequired = giftCardConfiguration?.isPinRequired ?: true,
        )
    }

    private fun GiftCardComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): GiftCardComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
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
