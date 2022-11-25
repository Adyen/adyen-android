/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount

internal class BacsDirectDebitComponentParamsMapper(
    private val parentConfiguration: Configuration?,
    private val isCreatedByDropIn: Boolean,
) {

    fun mapToParams(
        bacsDirectDebitConfiguration: BacsDirectDebitConfiguration,
    ): BacsDirectDebitComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: bacsDirectDebitConfiguration,
            bacsDirectDebitConfiguration = bacsDirectDebitConfiguration,
        )
    }

    private fun mapToParams(
        parentConfiguration: Configuration,
        bacsDirectDebitConfiguration: BacsDirectDebitConfiguration,
    ): BacsDirectDebitComponentParams {
        return BacsDirectDebitComponentParams(
            shopperLocale = parentConfiguration.shopperLocale,
            environment = parentConfiguration.environment,
            clientKey = parentConfiguration.clientKey,
            isAnalyticsEnabled = parentConfiguration.isAnalyticsEnabled ?: true,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = bacsDirectDebitConfiguration.amount ?: Amount.EMPTY,
        )
    }
}
