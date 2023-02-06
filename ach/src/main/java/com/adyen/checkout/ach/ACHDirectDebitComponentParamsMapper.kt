/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.ui.AddressParams

internal class ACHDirectDebitComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
) {

    fun mapToParams(
        configuration: ACHDirectDebitConfiguration
    ): ACHDirectDebitComponentParams {
        return configuration.mapToParamsInternal().override(overrideComponentParams)
    }

    private fun ACHDirectDebitConfiguration.mapToParamsInternal(): ACHDirectDebitComponentParams {
        return ACHDirectDebitComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            addressParams = addressConfiguration.mapToAddressParam()
        )
    }

    private fun AddressConfiguration.mapToAddressParam(): AddressParams {
        return when (this) {
            is AddressConfiguration.None -> {
                AddressParams.None
            }
            is AddressConfiguration.FullAddress -> {
                AddressParams.FullAddress(
                    supportedCountryCodes = supportedCountryCodes,
                    addressFieldPolicy = AddressFieldPolicyParams.Required
                )
            }
        }
    }

    private fun ACHDirectDebitComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): ACHDirectDebitComponentParams {
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
}
