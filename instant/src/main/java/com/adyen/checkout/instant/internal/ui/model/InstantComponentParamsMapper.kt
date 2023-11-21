/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2023.
 */

package com.adyen.checkout.instant.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.instant.getInstantPaymentConfiguration
import java.util.Locale

internal class InstantComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
    ): InstantComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )

        val instantActionConfiguration =
            paymentMethod.type?.let { checkoutConfiguration.getInstantPaymentConfiguration(it) }
                ?: checkoutConfiguration.getInstantPaymentConfiguration()

        return InstantComponentParams(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            shouldUseSdk = instantActionConfiguration?.shouldUseSdk ?: true,
        )
    }
}
