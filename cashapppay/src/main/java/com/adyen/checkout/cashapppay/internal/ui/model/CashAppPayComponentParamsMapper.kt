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
import com.adyen.checkout.core.exception.ComponentException

internal class CashAppPayComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        configuration: CashAppPayConfiguration,
        sessionsParams: SessionParams?,
    ): CashAppPayComponentParams = configuration
        .mapToParamsInternal()
        .override(overrideComponentParams)
        .override(sessionsParams ?: overrideSessionParams)

    private fun CashAppPayConfiguration.mapToParamsInternal() = CashAppPayComponentParams(
        isSubmitButtonVisible = isSubmitButtonVisible ?: true,
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        isAnalyticsEnabled = isAnalyticsEnabled ?: true,
        isCreatedByDropIn = false,
        amount = amount,
        cashAppPayEnvironment = cashAppPayEnvironment ?: getDefaultCashAppPayEnvironment(this),
        // TODO: Check if ok to throw exception
        returnUrl = returnUrl ?: throw ComponentException(
            "returnUrl is not configured. Make sure it is set in CashAppPayComponent.Builder"
        ),
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
        sessionsParams: SessionParams?,
    ): CashAppPayComponentParams {
        if (sessionsParams == null) return this
        return copy(
            amount = sessionsParams.amount ?: amount
            // TODO: Check if returnUrl can be overridden
        )
    }
}
