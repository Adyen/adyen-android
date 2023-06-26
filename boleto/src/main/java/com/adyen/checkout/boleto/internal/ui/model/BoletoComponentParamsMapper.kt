/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.boleto.BoletoConfiguration
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class BoletoComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: BoletoConfiguration,
        sessionParams: SessionParams?
    ): BoletoComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun BoletoConfiguration.mapToParamsInternal(): BoletoComponentParams {
        return BoletoComponentParams(
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            addressParams = AddressParams.FullAddress(
                defaultCountryCode = BRAZIL_COUNTRY_CODE,
                supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                addressFieldPolicy = AddressFieldPolicyParams.Required
            ),
            isEmailVisible = isEmailVisible ?: false
        )
    }

    private fun BoletoComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): BoletoComponentParams {
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

    private fun BoletoComponentParams.override(
        sessionParams: SessionParams?
    ): BoletoComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }

    companion object {
        private const val BRAZIL_COUNTRY_CODE = "BR"

        // this payment method only works for Brazil so we don't need other countries inside country drop down
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf(BRAZIL_COUNTRY_CODE)
    }
}
