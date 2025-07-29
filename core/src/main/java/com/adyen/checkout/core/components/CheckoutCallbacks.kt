/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class CheckoutCallbacks(
    private val beforeSubmit: BeforeSubmitCallback = BeforeSubmitCallback { false },
    private val onSubmit: OnSubmitCallback,
    private val onAdditionalDetails: OnAdditionalDetailsCallback,
    private val onError: OnErrorCallback,
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) {

    private val additionalCallbacks = mutableMapOf<KClass<out CheckoutCallback>, CheckoutCallback>()

    init {
        apply(additionalCallbacksBlock)
    }

    internal suspend fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean {
        return beforeSubmit.beforeSubmit(paymentComponentState)
    }

    internal suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return onSubmit.onSubmit(paymentComponentState)
    }

    internal suspend fun onAdditionalDetails(actionComponentData: ActionComponentData): CheckoutResult {
        return onAdditionalDetails.onAdditionalDetails(actionComponentData)
    }

    internal suspend fun onError() {
        onError.onError()
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : CheckoutCallback> addCallback(callback: T, clazz: KClass<T>) {
        additionalCallbacks[clazz] = callback
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : CheckoutCallback> getCallback(clazz: KClass<T>): T? {
        return additionalCallbacks[clazz]?.let { clazz.safeCast(it) }
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
    suspend fun onAdditionalDetails(actionComponentData: ActionComponentData): CheckoutResult
}

fun interface OnErrorCallback : CheckoutCallback {
    // TODO - add checkout error parameter once available
    suspend fun onError()
}
