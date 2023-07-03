/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment

internal class CashAppPayComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: CashAppPayConfiguration,
        sessionParams: SessionParams?,
    ): CashAppPayComponentParams = configuration
        .mapToParamsInternal()
        .override(overrideComponentParams)
        .override(sessionParams ?: overrideSessionParams)

    private fun CashAppPayConfiguration.mapToParamsInternal() = CashAppPayComponentParams(
        isSubmitButtonVisible = isSubmitButtonVisible ?: true,
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled ?: true,
        isCreatedByDropIn = false,
        amount = amount,
        cashAppPayEnvironment = cashAppPayEnvironment ?: getDefaultCashAppPayEnvironment(this),
        returnUrl = returnUrl,
        showStorePaymentField = showStorePaymentField ?: true,
        storePaymentMethod = storePaymentMethod ?: false,
    )

    private fun getDefaultCashAppPayEnvironment(configuration: CashAppPayConfiguration): CashAppPayEnvironment {
        return if (configuration.environment == Environment.TEST) {
            CashAppPayEnvironment.SANDBOX
        } else {
            CashAppPayEnvironment.PRODUCTION
        }
    }

    private fun CashAppPayComponentParams.override(
        overrideComponentParams: ComponentParams?,
    ): CashAppPayComponentParams {
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

    private fun CashAppPayComponentParams.override(
        sessionParams: SessionParams?,
    ): CashAppPayComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
            showStorePaymentField = sessionParams.enableStoreDetails ?: showStorePaymentField,
            returnUrl = sessionParams.returnUrl,
        )
    }
}
