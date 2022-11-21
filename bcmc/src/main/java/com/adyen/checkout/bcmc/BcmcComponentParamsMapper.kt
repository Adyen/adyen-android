/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.components.base.Configuration

internal class BcmcComponentParamsMapper(
    private val parentConfiguration: Configuration?
) {

    fun mapToParams(
        bcmcConfiguration: BcmcConfiguration,
    ): BcmcComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: bcmcConfiguration,
            bcmcConfiguration = bcmcConfiguration,
        )
    }

    private fun mapToParams(
        parentConfiguration: Configuration,
        bcmcConfiguration: BcmcConfiguration,
    ): BcmcComponentParams {
        with(bcmcConfiguration) {
            return BcmcComponentParams(
                shopperLocale = parentConfiguration.shopperLocale,
                environment = parentConfiguration.environment,
                clientKey = parentConfiguration.clientKey,
                isHolderNameRequired = isHolderNameRequired ?: false,
                shopperReference = shopperReference,
                isStorePaymentFieldVisible = isStorePaymentFieldVisible ?: false,
            )
        }
    }
}
