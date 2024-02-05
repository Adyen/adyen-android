/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.boleto.getBoletoConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class BoletoComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: CheckoutConfiguration,
        sessionParams: SessionParams?
    ): BoletoComponentParams {
        return configuration
            .mapToParamsInternal()
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(): BoletoComponentParams {
        val boletoConfiguration = getBoletoConfiguration()
        return BoletoComponentParams(
            isSubmitButtonVisible = boletoConfiguration?.isSubmitButtonVisible ?: true,
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            addressParams = AddressParams.FullAddress(
                defaultCountryCode = BRAZIL_COUNTRY_CODE,
                supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                addressFieldPolicy = AddressFieldPolicyParams.Required,
            ),
            isEmailVisible = boletoConfiguration?.isEmailVisible ?: false,
        )
    }

    private fun BoletoComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): BoletoComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun BoletoComponentParams.override(
        sessionParams: SessionParams?
    ): BoletoComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
            shopperLocale = sessionParams.shopperLocale ?: shopperLocale,
        )
    }

    companion object {
        private const val BRAZIL_COUNTRY_CODE = "BR"

        // this payment method only works for Brazil so we don't need other countries inside country drop down
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf(BRAZIL_COUNTRY_CODE)
    }
}
