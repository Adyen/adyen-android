/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class IssuerListComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
    private val hideIssuerLogosDefaultValue: Boolean = false,
) {

    fun mapToParams(
        issuerListConfiguration: IssuerListConfiguration,
        sessionParams: SessionParams?,
    ): IssuerListComponentParams {
        return issuerListConfiguration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun IssuerListConfiguration.mapToParamsInternal(): IssuerListComponentParams {
        return IssuerListComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
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
            analyticsParams = overrideComponentParams.analyticsParams,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun IssuerListComponentParams.override(
        sessionParams: SessionParams? = null
    ): IssuerListComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
