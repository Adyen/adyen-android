/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.model

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.adyen3ds2.getAdyen3DS2Configuration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams

internal class Adyen3DS2ComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): Adyen3DS2ComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal()
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(): Adyen3DS2ComponentParams {
        val adyen3ds2Configuration = getAdyen3DS2Configuration()
        return Adyen3DS2ComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            uiCustomization = adyen3ds2Configuration?.uiCustomization,
            threeDSRequestorAppURL = adyen3ds2Configuration?.threeDSRequestorAppURL,
            // Hardcoded for now, but in the feature we could make this configurable
            deviceParameterBlockList = DEVICE_PARAMETER_BLOCK_LIST,
        )
    }

    private fun Adyen3DS2ComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): Adyen3DS2ComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun Adyen3DS2ComponentParams.override(
        sessionParams: SessionParams?
    ): Adyen3DS2ComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
            shopperLocale = sessionParams.shopperLocale ?: shopperLocale,
        )
    }

    companion object {
        private const val PHONE_NUMBER_PARAMETER = "A005"

        @VisibleForTesting
        internal val DEVICE_PARAMETER_BLOCK_LIST = setOf(PHONE_NUMBER_PARAMETER)
    }
}
