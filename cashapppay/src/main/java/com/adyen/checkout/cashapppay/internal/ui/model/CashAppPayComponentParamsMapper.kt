/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import android.content.Context
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.cashapppay.getCashAppPayConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException

internal class CashAppPayComponentParamsMapper(
    private val isCreatedByDropIn: Boolean,
    private val overrideSessionParams: SessionParams?,
) {

    @Suppress("ThrowsCount")
    fun mapToParams(
        configuration: CheckoutConfiguration,
        sessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
        context: Context,
    ): CashAppPayComponentParams {
        val params = configuration
            .mapToParamsInternal(
                clientId = paymentMethod.configuration?.clientId ?: throw ComponentException(
                    "Cannot launch Cash App Pay, clientId is missing in the payment method object.",
                ),
                scopeId = paymentMethod.configuration?.scopeId ?: throw ComponentException(
                    "Cannot launch Cash App Pay, scopeId is missing in the payment method object.",
                ),
                context = context,
            )
            .override(sessionParams ?: overrideSessionParams)

        if (params.returnUrl == null) {
            throw ComponentException(
                "Cannot launch Cash App Pay, set the returnUrl in your CashAppPayConfiguration.Builder",
            )
        }

        return params
    }

    fun mapToParams(
        configuration: CheckoutConfiguration,
        sessionParams: SessionParams?,
        @Suppress("UNUSED_PARAMETER") paymentMethod: StoredPaymentMethod,
        context: Context,
    ): CashAppPayComponentParams = configuration
        // clientId and scopeId are not needed in the stored flow.
        .mapToParamsInternal(null, null, context)
        .override(sessionParams ?: overrideSessionParams)

    private fun CheckoutConfiguration.mapToParamsInternal(
        clientId: String?,
        scopeId: String?,
        context: Context,
    ): CashAppPayComponentParams {
        val cashAppPayConfiguration = getCashAppPayConfiguration()
        return CashAppPayComponentParams(
            isSubmitButtonVisible = cashAppPayConfiguration?.isSubmitButtonVisible ?: true,
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
            cashAppPayEnvironment = getCashAppPayEnvironment(cashAppPayConfiguration),
            // Take the configured returnUrl or create a default value for drop-in if it's null
            returnUrl = cashAppPayConfiguration?.returnUrl ?: if (isCreatedByDropIn) {
                CashAppPayComponent.getReturnUrl(context)
            } else {
                null
            },
            showStorePaymentField = cashAppPayConfiguration?.showStorePaymentField ?: true,
            storePaymentMethod = cashAppPayConfiguration?.storePaymentMethod ?: false,
            clientId = clientId,
            scopeId = scopeId,
        )
    }

    private fun CheckoutConfiguration.getCashAppPayEnvironment(
        cashAppPayConfiguration: CashAppPayConfiguration?
    ): CashAppPayEnvironment {
        return when {
            cashAppPayConfiguration?.cashAppPayEnvironment != null -> cashAppPayConfiguration.cashAppPayEnvironment
            environment == Environment.TEST -> CashAppPayEnvironment.SANDBOX
            else -> CashAppPayEnvironment.PRODUCTION
        }
    }

    private fun CashAppPayComponentParams.override(
        sessionParams: SessionParams?,
    ): CashAppPayComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
            showStorePaymentField = sessionParams.enableStoreDetails ?: showStorePaymentField,
            returnUrl = sessionParams.returnUrl ?: returnUrl,
        )
    }
}
