/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.ach.ACHDirectDebitAddressConfiguration
import com.adyen.checkout.ach.getACHDirectDebitConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

internal class ACHDirectDebitComponentParamsMapper(
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): ACHDirectDebitComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal()
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(): ACHDirectDebitComponentParams {
        val achDirectDebitConfiguration = getACHDirectDebitConfiguration()
        return ACHDirectDebitComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = achDirectDebitConfiguration?.isSubmitButtonVisible ?: true,
            addressParams = achDirectDebitConfiguration?.addressConfiguration?.mapToAddressParam()
                ?: AddressParams.FullAddress(
                    supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                    addressFieldPolicy = AddressFieldPolicyParams.Required,
                ),
            isStorePaymentFieldVisible = achDirectDebitConfiguration?.isStorePaymentFieldVisible ?: true,
        )
    }

    private fun ACHDirectDebitAddressConfiguration.mapToAddressParam(): AddressParams {
        return when (this) {
            is ACHDirectDebitAddressConfiguration.None -> {
                AddressParams.None
            }

            is ACHDirectDebitAddressConfiguration.FullAddress -> {
                AddressParams.FullAddress(
                    supportedCountryCodes = supportedCountryCodes,
                    addressFieldPolicy = AddressFieldPolicyParams.Required,
                )
            }
        }
    }

    private fun ACHDirectDebitComponentParams.override(
        dropInOverrideParams: DropInOverrideParams?,
    ): ACHDirectDebitComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun ACHDirectDebitComponentParams.override(
        sessionParams: SessionParams? = null
    ): ACHDirectDebitComponentParams {
        if (sessionParams == null) return this
        return copy(
            isStorePaymentFieldVisible = sessionParams.enableStoreDetails ?: isStorePaymentFieldVisible,
            amount = sessionParams.amount ?: amount,
            shopperLocale = sessionParams.shopperLocale ?: shopperLocale,
        )
    }

    companion object {
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf("US", "PR")
    }
}
