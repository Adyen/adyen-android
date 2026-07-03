/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.authentication.internal.ui.model

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.authentication.AuthenticationConfiguration
import com.adyen.checkout.core.common.internal.CheckoutParams

internal class AuthenticationComponentParamsMapper {

    fun mapToParams(
        params: CheckoutParams,
    ): AuthenticationComponentParams {
        val adyen3ds2Configuration = params.getConfiguration<AuthenticationConfiguration>()
        return AuthenticationComponentParams(
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
