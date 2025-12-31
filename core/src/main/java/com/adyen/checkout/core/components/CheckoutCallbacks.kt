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
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.common.exception.CheckoutError
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class CheckoutCallbacks(
    internal val beforeSubmit: BeforeSubmitCallback? = null,
    internal val onSubmit: OnSubmitCallback? = null,
    internal val onAdditionalDetails: OnAdditionalDetailsCallback? = null,
    internal val onError: OnErrorCallback? = null,
    internal val onFinished: OnFinishedCallback? = null,
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit = {},
) {

    private val additionalCallbacks = mutableMapOf<KClass<out CheckoutCallback>, CheckoutCallback>()

    init {
        apply(additionalCallbacksBlock)
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
    fun onError(error: CheckoutError)
}

fun interface OnFinishedCallback : CheckoutCallback {
    fun onFinished(paymentResult: PaymentResult)
}
