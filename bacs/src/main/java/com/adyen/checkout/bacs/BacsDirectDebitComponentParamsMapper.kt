/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount

internal class BacsDirectDebitComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
) {

    fun mapToParams(
        bacsDirectDebitConfiguration: BacsDirectDebitConfiguration,
    ): BacsDirectDebitComponentParams {
        return bacsDirectDebitConfiguration
            .mapToParamsInternal()
            .override(overrideComponentParams)
    }

    private fun BacsDirectDebitConfiguration.mapToParamsInternal(): BacsDirectDebitComponentParams {
        return BacsDirectDebitComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount ?: Amount.EMPTY,
        )
    }

    private fun BacsDirectDebitComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): BacsDirectDebitComponentParams {
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
