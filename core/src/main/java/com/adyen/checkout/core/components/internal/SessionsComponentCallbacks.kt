/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/8/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.common.exception.CheckoutError
import com.adyen.checkout.core.components.BeforeSubmitCallback
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.OnAdditionalDetailsCallback
import com.adyen.checkout.core.components.OnErrorCallback
import com.adyen.checkout.core.components.OnFinishedCallback
import com.adyen.checkout.core.components.OnSubmitCallback
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal class SessionsComponentCallbacks(
    private val beforeSubmit: BeforeSubmitCallback?,
    val onSubmit: OnSubmitCallback?,
    val onAdditionalDetails: OnAdditionalDetailsCallback?,
    private val onError: OnErrorCallback,
    private val onFinished: OnFinishedCallback?,
) {
    suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean? {
        return beforeSubmit?.beforeSubmit(paymentComponentState)
    }

    suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return onSubmit?.onSubmit(paymentComponentState) ?: error("onSubmit() callback is not set.")
    }

    suspend fun onAdditionalDetails(actionComponentData: ActionComponentData): CheckoutResult {
        return onAdditionalDetails?.onAdditionalDetails(actionComponentData)
            ?: error("onAdditionalDetails() callback is not set.")
    }

    fun onError(error: CheckoutError) {
        onError.onError(error)
    }

    fun onFinished(paymentResult: PaymentResult) {
        onFinished?.onFinished(paymentResult)
    }
}

internal fun CheckoutCallbacks.toSessionsComponentCallbacks(): SessionsComponentCallbacks {
    return SessionsComponentCallbacks(
        beforeSubmit = beforeSubmit,
        onSubmit = onSubmit,
        onAdditionalDetails = onAdditionalDetails,
        onError = onError ?: error("onError() callback is not set."),
        onFinished = onFinished,
    )
}
