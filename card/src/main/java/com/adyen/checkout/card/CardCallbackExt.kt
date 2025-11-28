/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.internal.CardCallbacks
import com.adyen.checkout.core.components.CheckoutCallbacks

fun CheckoutCallbacks.card(initBlock: CardCallbacks.() -> Unit) {
    apply {
        val callbacks = CardCallbacks().apply(initBlock)
        callbacks.onBinValue?.let { addCallback(it, OnBinValueCallback::class) }
        callbacks.onBinLookup?.let { addCallback(it, OnBinLookupCallback::class) }
    }
}

fun CardCallbacks.onBinValue(onBinValueCallback: OnBinValueCallback) {
    onBinValue = onBinValueCallback
}

fun CardCallbacks.onBinLookup(onBinLookupCallback: OnBinLookupCallback) {
    onBinLookup = onBinLookupCallback
}
