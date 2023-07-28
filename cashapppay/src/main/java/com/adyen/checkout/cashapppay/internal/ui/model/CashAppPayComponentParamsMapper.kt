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
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException

internal class CashAppPayComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    @Suppress("ThrowsCount")
    fun mapToParams(
        configuration: CashAppPayConfiguration,
        sessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
    ): CashAppPayComponentParams {
        val params = configuration
            .mapToParamsInternal(
                clientId = paymentMethod.configuration?.clientId ?: throw ComponentException(
                    "Cannot launch Cash App Pay, clientId is missing in the payment method object."
                ),
                scopeId = paymentMethod.configuration?.scopeId ?: throw ComponentException(
                    "Cannot launch Cash App Pay, scopeId is missing in the payment method object."
                ),
            )
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)

        if (params.returnUrl == null) {
            throw ComponentException(
                "Cannot launch Cash App Pay, set the returnUrl in your CashAppPayConfiguration.Builder"
            )
        }

        return params
    }

    fun mapToParams(
        configuration: CashAppPayConfiguration,
        sessionParams: SessionParams?,
        @Suppress("UNUSED_PARAMETER") paymentMethod: StoredPaymentMethod,
    ): CashAppPayComponentParams = configuration
        // clientId and scopeId are not needed in the stored flow.
        .mapToParamsInternal(null, null)
        .override(overrideComponentParams)
        .override(sessionParams ?: overrideSessionParams)

    private fun CashAppPayConfiguration.mapToParamsInternal(
        clientId: String?,
        scopeId: String?,
    ) = CashAppPayComponentParams(
        isSubmitButtonVisible = isSubmitButtonVisible ?: true,
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = AnalyticsParams(analyticsConfiguration),
        isCreatedByDropIn = false,
        amount = amount,
        cashAppPayEnvironment = getCashAppPayEnvironment(),
        returnUrl = returnUrl,
        showStorePaymentField = showStorePaymentField ?: true,
        storePaymentMethod = storePaymentMethod ?: false,
        clientId = clientId,
        scopeId = scopeId,
    )

    private fun CashAppPayConfiguration.getCashAppPayEnvironment(): CashAppPayEnvironment {
        return when {
            cashAppPayEnvironment != null -> cashAppPayEnvironment
            environment == Environment.TEST -> CashAppPayEnvironment.SANDBOX
            else -> CashAppPayEnvironment.PRODUCTION
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
            analyticsParams = overrideComponentParams.analyticsParams,
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
            returnUrl = sessionParams.returnUrl ?: returnUrl
        )
    }
}
