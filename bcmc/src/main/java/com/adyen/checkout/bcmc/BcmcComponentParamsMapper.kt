/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.components.base.ComponentParams

internal class BcmcComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
) {

    fun mapToParams(
        bcmcConfiguration: BcmcConfiguration,
    ): BcmcComponentParams {
        return bcmcConfiguration
            .mapToParamsInternal()
            .override(overrideComponentParams)
    }

    private fun BcmcConfiguration.mapToParamsInternal(): BcmcComponentParams {
        return BcmcComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            isHolderNameRequired = isHolderNameRequired ?: false,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = isStorePaymentFieldVisible ?: false,
        )
    }

    private fun BcmcComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): BcmcComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
        )
    }
}
