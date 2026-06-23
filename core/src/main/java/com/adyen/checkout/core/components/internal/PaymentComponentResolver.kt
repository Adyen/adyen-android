/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import kotlinx.coroutines.CoroutineScope

internal object PaymentComponentResolver {

    fun resolve(
        paymentMethod: PaymentMethodResponse,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        return when (paymentMethod) {
            is PaymentMethod -> resolvePaymentMethod(
                paymentMethod = paymentMethod,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                sdkDataProvider = sdkDataProvider,
                checkoutParams = checkoutParams,
            )

            is StoredPaymentMethod -> resolveStoredPaymentMethod(
                paymentMethod = paymentMethod,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                sdkDataProvider = sdkDataProvider,
                checkoutParams = checkoutParams,
            )

            else -> PaymentComponentResult.Failure("Unsupported payment method response type.")
        }
    }

    private fun resolvePaymentMethod(
        paymentMethod: PaymentMethod,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val component = PaymentMethodProvider.getPaymentComponent(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = checkoutParams,
            additionalCallbacks = callbacks.additionalCallbacks,
        ) ?: return PaymentComponentResult.Failure(
            "Payment method '${paymentMethod.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }

    private fun resolveStoredPaymentMethod(
        paymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val component = PaymentMethodProvider.getStoredPaymentComponent(
            storedPaymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = checkoutParams,
        ) ?: return PaymentComponentResult.Failure(
            "Stored payment method type '${paymentMethod.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }
}
