/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.ach.ACHDirectDebitAddressConfiguration
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.ach.getACHDirectDebitConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import java.util.Locale

internal class ACHDirectDebitComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): ACHDirectDebitComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val achDirectDebitConfiguration = checkoutConfiguration.getACHDirectDebitConfiguration()
        return mapToParams(
            commonComponentParamsMapperData.commonComponentParams,
            commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams,
            achDirectDebitConfiguration,
            checkoutConfiguration,
        )
    }

    private fun mapToParams(
        commonComponentParams: CommonComponentParams,
        sessionParams: SessionParams?,
        dropInOverrideParams: DropInOverrideParams?,
        achDirectDebitConfiguration: ACHDirectDebitConfiguration?,
        checkoutConfiguration: CheckoutConfiguration,
    ): ACHDirectDebitComponentParams {
        return ACHDirectDebitComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: achDirectDebitConfiguration?.isSubmitButtonVisible
                ?: checkoutConfiguration.isSubmitButtonVisible
                ?: true,
            addressParams = achDirectDebitConfiguration?.addressConfiguration?.mapToAddressParam()
                ?: AddressParams.FullAddress(
                    supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                    addressFieldPolicy = AddressFieldPolicyParams.Required,
                ),
            isStorePaymentFieldVisible = sessionParams?.enableStoreDetails
                ?: achDirectDebitConfiguration?.isStorePaymentFieldVisible ?: true,
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

    companion object {
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf("US", "PR")
    }
}
