/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

class CheckoutCallbacks(
    private val beforeSubmit: BeforeSubmitCallback = BeforeSubmitCallback { false },
    private val onSubmit: OnSubmitCallback,
    private val onAdditionalDetails: OnAdditionalDetailsCallback,
    private val onError: OnErrorCallback,
) {

    internal suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean {
        return beforeSubmit.beforeSubmit(paymentComponentState)
    }

    internal suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return onSubmit.onSubmit(paymentComponentState)
    }

    internal suspend fun onAdditionalDetails(): CheckoutResult {
        return onAdditionalDetails.onAdditionalDetails()
    }

    internal suspend fun onError() {
        onError.onError()
    }
}

interface CheckoutCallback

fun interface BeforeSubmitCallback : CheckoutCallback {
    suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean
}

fun interface OnSubmitCallback : CheckoutCallback {
    suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult
}

fun interface OnAdditionalDetailsCallback : CheckoutCallback {
    // TODO - add action component state parameter once available
    suspend fun onAdditionalDetails(): CheckoutResult
}

fun interface OnErrorCallback : CheckoutCallback {
    // TODO - add checkout error parameter once available
    suspend fun onError()
}
