/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.model

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.adyen3ds2.getAdyen3DS2Configuration
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams

internal class Adyen3DS2ComponentParamsMapper {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        commonComponentParams: CommonComponentParams,
    ): Adyen3DS2ComponentParams {
        val adyen3ds2Configuration = checkoutConfiguration.getAdyen3DS2Configuration()
        return Adyen3DS2ComponentParams(
            commonComponentParams = commonComponentParams,
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
