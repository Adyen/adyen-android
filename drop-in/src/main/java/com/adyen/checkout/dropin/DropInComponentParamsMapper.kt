/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.components.core.Amount

internal class DropInComponentParamsMapper {

    fun mapToParams(
        dropInConfiguration: DropInConfiguration,
        overrideAmount: Amount,
    ): DropInComponentParams {
        with(dropInConfiguration) {
            return DropInComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                isAnalyticsEnabled = isAnalyticsEnabled ?: true,
                isCreatedByDropIn = true,
                amount = overrideAmount,
                showPreselectedStoredPaymentMethod = showPreselectedStoredPaymentMethod,
                skipListWhenSinglePaymentMethod = skipListWhenSinglePaymentMethod,
                isRemovingStoredPaymentMethodsEnabled = isRemovingStoredPaymentMethodsEnabled,
                additionalDataForDropInService = additionalDataForDropInService,
            )
        }
    }
}
