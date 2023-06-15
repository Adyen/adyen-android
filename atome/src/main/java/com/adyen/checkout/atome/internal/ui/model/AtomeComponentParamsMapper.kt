/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2023.
 */

package com.adyen.checkout.atome.internal.ui.model

import com.adyen.checkout.atome.AtomeConfiguration
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class AtomeComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: AtomeConfiguration,
        sessionParams: SessionParams?,
    ): AtomeComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun AtomeConfiguration.mapToParamsInternal(): AtomeComponentParams {
        return AtomeComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            addressParams = AddressParams.FullAddress(
                supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                addressFieldPolicy = AddressFieldPolicyParams.Required
            )
        )
    }

    private fun AtomeComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): AtomeComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun AtomeComponentParams.override(
        sessionParams: SessionParams? = null
    ): AtomeComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }

    companion object {
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf("ID", "PH", "TH", "VN", "JP", "TW", "KR", "SG", "MY", "HK")
    }
}
