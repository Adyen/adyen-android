/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.ComponentParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class IssuerListComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val hideIssuerLogosDefaultValue: Boolean = false,
) {

    fun mapToParams(
        issuerListConfiguration: IssuerListConfiguration
    ): IssuerListComponentParams {
        return issuerListConfiguration
            .mapToParamsInternal()
            .override(overrideComponentParams)
    }

    private fun IssuerListConfiguration.mapToParamsInternal(): IssuerListComponentParams {
        return IssuerListComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            viewType = viewType ?: IssuerListViewType.RECYCLER_VIEW,
            hideIssuerLogos = hideIssuerLogos ?: hideIssuerLogosDefaultValue,
        )
    }

    private fun IssuerListComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): IssuerListComponentParams {
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
