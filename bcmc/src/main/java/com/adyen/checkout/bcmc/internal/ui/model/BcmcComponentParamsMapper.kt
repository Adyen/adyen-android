/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

internal class BcmcComponentParamsMapper {

    fun mapToParams(
        bcmcConfiguration: BcmcConfiguration,
        overrideComponentParams: ComponentParams? = null,
        sessionSetupConfiguration: SessionSetupConfiguration? = null
    ): BcmcComponentParams {
        return bcmcConfiguration
            .mapToParamsInternal(sessionSetupConfiguration)
            .override(overrideComponentParams)
    }

    private fun BcmcConfiguration.mapToParamsInternal(
        sessionSetupConfiguration: SessionSetupConfiguration? = null
    ): BcmcComponentParams {
        return BcmcComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            isHolderNameRequired = isHolderNameRequired ?: false,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = sessionSetupConfiguration?.enableStoreDetails
                ?: isStorePaymentFieldVisible ?: false,
        )
    }

    private fun BcmcComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): BcmcComponentParams {
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
