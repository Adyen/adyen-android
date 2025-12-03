/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.threeds2.old.internal.ui.model

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.threeds2.old.getAdyen3DS2Configuration
import java.util.Locale

internal class Adyen3DS2ComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
    ): Adyen3DS2ComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val adyen3ds2Configuration = checkoutConfiguration.getAdyen3DS2Configuration()
        return Adyen3DS2ComponentParams(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            uiCustomization = adyen3ds2Configuration?.uiCustomization,
            threeDSRequestorAppURL = adyen3ds2Configuration?.threeDSRequestorAppURL,
            // Hardcoded for now, but in the feature we could make this configurable
            deviceParameterBlockList = DEVICE_PARAMETER_BLOCK_LIST,
        )
    }

    companion object {
        private const val PHONE_NUMBER_PARAMETER = "A005"

        @VisibleForTesting
        internal val DEVICE_PARAMETER_BLOCK_LIST = setOf(PHONE_NUMBER_PARAMETER)
    }
}
