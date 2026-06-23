/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import kotlinx.coroutines.CoroutineScope

internal object PaymentComponentResolver {

    @Suppress("LongParameterList")
    fun resolve(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> resolvePaymentMethod(
                target = target,
                context = context,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                sdkDataProvider = sdkDataProvider,
                checkoutParams = checkoutParams,
            )

            is CheckoutTarget.StoredPaymentMethod -> resolveStoredPaymentMethod(
                target = target,
                context = context,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                sdkDataProvider = sdkDataProvider,
                checkoutParams = checkoutParams,
            )

            else -> PaymentComponentResult.Failure("Unsupported checkout target.")
        }
    }

    @Suppress("LongParameterList", "ReturnCount")
    private fun resolvePaymentMethod(
        target: CheckoutTarget.PaymentMethod,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val paymentMethods = context.getPaymentMethodResponse()?.paymentMethods
            ?: return PaymentComponentResult.Failure("No payment methods response available.")

        val paymentMethod = paymentMethods.find { it.type == target.type }
            ?: return PaymentComponentResult.Failure(
                "Payment method '${target.type}' was not found in the payment methods response.",
            )

        val component = PaymentMethodProvider.getPaymentComponent(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = checkoutParams,
            additionalCallbacks = callbacks.additionalCallbacks,
        ) ?: return PaymentComponentResult.Failure(
            "Payment method '${target.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }

    @Suppress("ReturnCount", "LongParameterList")
    private fun resolveStoredPaymentMethod(
        target: CheckoutTarget.StoredPaymentMethod,
        context: CheckoutContext,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val storedPaymentMethods = context.getPaymentMethodResponse()?.storedPaymentMethods
            ?: return PaymentComponentResult.Failure("No payment methods response available.")

        val storedPaymentMethod = storedPaymentMethods.find { it.id == target.id }
            ?: return PaymentComponentResult.Failure(
                "Stored payment method with id '${target.id}' was not found in the payment methods response.",
            )

        val component = PaymentMethodProvider.getStoredPaymentComponent(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = checkoutParams,
        ) ?: return PaymentComponentResult.Failure(
            "Stored payment method type '${storedPaymentMethod.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }

    private fun CheckoutContext.getPaymentMethodResponse(): PaymentMethods? {
        return when (this) {
            is CheckoutContext.Advanced -> paymentMethods
            is CheckoutContext.Sessions -> checkoutSession.sessionSetupResponse.paymentMethods
            is CheckoutContext.ActionOnly -> null
        }
    }
}
