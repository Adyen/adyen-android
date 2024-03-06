/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/2/2024.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CommonComponentParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): CommonComponentParamsMapperData {
        val sessionParams: SessionParams? = dropInOverrideParams?.sessionParams ?: componentSessionParams
        val commonComponentParams = CommonComponentParams(
            shopperLocale = checkoutConfiguration.shopperLocale ?: sessionParams?.shopperLocale ?: deviceLocale,
            environment = sessionParams?.environment ?: checkoutConfiguration.environment,
            clientKey = sessionParams?.clientKey ?: checkoutConfiguration.clientKey,
            analyticsParams = AnalyticsParams(
                analyticsConfiguration = checkoutConfiguration.analyticsConfiguration,
                clientKey = checkoutConfiguration.clientKey,
            ),
            isCreatedByDropIn = dropInOverrideParams != null,
            amount = sessionParams?.amount
                ?: dropInOverrideParams?.amount
                ?: checkoutConfiguration.amount,
        )
        return CommonComponentParamsMapperData(commonComponentParams, sessionParams)
    }
}
