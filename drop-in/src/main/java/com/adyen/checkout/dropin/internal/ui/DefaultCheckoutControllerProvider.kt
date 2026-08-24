/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.BeforeSubmitResult
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SessionCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.BeforeSubmitData
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class DefaultCheckoutControllerProvider(
    private val checkoutContext: CheckoutContext,
    private val dropInServiceManager: DropInServiceManager,
) : CheckoutControllerProvider {

    override fun provide(
        paymentFlowType: DropInPaymentFlowType,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val target = when (paymentFlowType) {
            is DropInPaymentFlowType.RegularPaymentMethod -> {
                CheckoutTarget.PaymentMethod(type = paymentFlowType.txVariant)
            }

            is DropInPaymentFlowType.StoredPaymentMethod -> {
                CheckoutTarget.StoredPaymentMethod(id = paymentFlowType.id)
            }
        }

        return when (checkoutContext) {
            is CheckoutContext.Advanced -> {
                CheckoutController(
                    target = target,
                    context = checkoutContext,
                    callbacks = AdvancedCheckoutCallbacks(
                        onSubmit = ::onSubmit,
                        onAdditionalDetails = ::onAdditionalDetails,
                        onFailure = { error -> onFailure(error, coroutineScope) },
                    ),
                    coroutineScope = coroutineScope,
                )
            }

            is CheckoutContext.Sessions -> {
                CheckoutController(
                    target = target,
                    context = checkoutContext,
                    callbacks = SessionCheckoutCallbacks(
                        onBeforeSubmit = ::onBeforeSubmit,
                        onFailure = { error -> onFailure(error, coroutineScope) },
                        onComplete = { result -> onComplete(result, coroutineScope) },
                    ),
                    coroutineScope = coroutineScope,
                )
            }

            is CheckoutContext.ActionOnly -> error("Unsupported context: $checkoutContext")
        }
    }

    private suspend fun onBeforeSubmit(data: BeforeSubmitData): BeforeSubmitResult {
        // TODO - Implement after beforeSubmit is added to DropInService
        return BeforeSubmitResult.Proceed(data)
    }

    private suspend fun onSubmit(paymentComponentData: PaymentComponentData<*>): SubmitResult {
        return dropInServiceManager.requestOnSubmit(paymentComponentData)
    }

    private suspend fun onAdditionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        return dropInServiceManager.requestOnAdditionalDetails(data)
    }

    private fun onFailure(error: CheckoutError, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            dropInServiceManager.onFailure(error)
        }
    }

    private fun onComplete(result: SessionCheckoutResult, coroutineScope: CoroutineScope) {
        // TODO - Implement after signature of onFinished is updated
        coroutineScope.launch {
            dropInServiceManager.onPaymentCompleted(result.resultCode)
        }
    }
}
