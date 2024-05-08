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
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import java.util.Locale

internal class BoletoComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): BoletoComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val boletoConfiguration = checkoutConfiguration.getBoletoConfiguration()
        val commonComponentParams = commonComponentParamsMapperData.commonComponentParams

        return BoletoComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: boletoConfiguration?.isSubmitButtonVisible ?: true,
            addressParams = AddressParams.FullAddress(
                defaultCountryCode = BRAZIL_COUNTRY_CODE,
                supportedCountryCodes = DEFAULT_SUPPORTED_COUNTRY_LIST,
                addressFieldPolicy = AddressFieldPolicyParams.Required,
            ),
            isEmailVisible = boletoConfiguration?.isEmailVisible ?: false,
        )
    }

    companion object {
        private const val BRAZIL_COUNTRY_CODE = "BR"

        // this payment method only works for Brazil so we don't need other countries inside country drop down
        private val DEFAULT_SUPPORTED_COUNTRY_LIST = listOf(BRAZIL_COUNTRY_CODE)
    }
}
