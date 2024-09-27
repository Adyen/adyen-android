/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui.model

import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.twint.TwintConfiguration
import com.adyen.checkout.twint.getTwintConfiguration
import java.util.Locale

internal class TwintComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): TwintComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )

        val twintConfiguration = checkoutConfiguration.getTwintConfiguration()

        return mapToParamsInternal(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            sessionParams = commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams = dropInOverrideParams,
            twintConfiguration = twintConfiguration,
        )
    }

    private fun mapToParamsInternal(
        commonComponentParams: CommonComponentParams,
        sessionParams: SessionParams?,
        dropInOverrideParams: DropInOverrideParams?,
        twintConfiguration: TwintConfiguration?,
    ): TwintComponentParams {
        return TwintComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: twintConfiguration?.isSubmitButtonVisible ?: true,
            showStorePaymentField = getShowStorePaymentField(sessionParams, twintConfiguration),
            actionHandlingMethod = twintConfiguration?.actionHandlingMethod ?: ActionHandlingMethod.PREFER_NATIVE,
        )
    }

    private fun getShowStorePaymentField(
        sessionParams: SessionParams?,
        twintConfiguration: TwintConfiguration?,
    ): Boolean {
        return sessionParams?.enableStoreDetails ?: twintConfiguration?.showStorePaymentField ?: true
    }
}
