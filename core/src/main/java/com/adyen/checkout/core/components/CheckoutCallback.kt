/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

//interface CheckoutCallback {
//
//    fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean = false
//
//    fun onSubmit(
//        paymentComponentState: PaymentComponentState<*>,
//        onCompletion: (CheckoutResult) -> Unit
//    )
//}

class CheckoutCallback(
    onSubmit: OnSubmitCallback,
    beforeSubmit: BeforeSubmitCallback = BeforeSubmitCallback { false },
    additionalCallbacks: CheckoutCallback.() -> Unit = {},
) {

    private val callbacks = mutableMapOf(
        "beforeSubmit" to beforeSubmit,
        "onSubmit" to onSubmit,
    )

    init {
        apply(additionalCallbacks)
    }

    internal fun addCallback(name: String, callback: Callback) {
        callbacks[name] = callback
    }

    internal fun <T : Callback> getCallback(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return callbacks[name] as? T?
    }
}

interface Callback

fun interface BeforeSubmitCallback : Callback {
    fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean
}

fun interface OnSubmitCallback : Callback {
    fun onSubmit(paymentComponentState: PaymentComponentState<*>)
}

fun interface OnBinLookupCallback : Callback {
    fun onBinLookup(bin: String)
}

fun CheckoutCallback.onBinLookup(onBinLookup: OnBinLookupCallback) {
    addCallback("onBinLookup", onBinLookup)
}

object ExampleImplementation {

    val checkoutCallback = CheckoutCallback(
        onSubmit = {},
    ) {
        onBinLookup { bin ->
            // Do something with the bin
        }
    }
}
