/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class IssuerListComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val hideIssuerLogosDefaultValue: Boolean = false,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        configuration: IssuerListConfiguration?,
        sessionParams: SessionParams?,
    ): IssuerListComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal(configuration)
            .override(dropInOverrideParams)
            .override(sessionParams ?: dropInOverrideParams?.sessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(
        configuration: IssuerListConfiguration?
    ): IssuerListComponentParams {
        return IssuerListComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = configuration?.isSubmitButtonVisible ?: true,
            viewType = configuration?.viewType ?: IssuerListViewType.RECYCLER_VIEW,
            hideIssuerLogos = configuration?.hideIssuerLogos ?: hideIssuerLogosDefaultValue,
        )
    }

    private fun IssuerListComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): IssuerListComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun IssuerListComponentParams.override(
        sessionParams: SessionParams?,
    ): IssuerListComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }
}
