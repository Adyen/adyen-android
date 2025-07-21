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
) {

    internal suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean {
        return beforeSubmit.beforeSubmit(paymentComponentState)
    }

    internal suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return onSubmit.onSubmit(paymentComponentState)
    }
}

interface CheckoutCallback

fun interface BeforeSubmitCallback : CheckoutCallback {
    suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean
}

fun interface OnSubmitCallback : CheckoutCallback {
    suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult
}
