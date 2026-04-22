/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo

abstract class CheckoutCallbacks(
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit,
) {

    private val _additionalCallbacks = mutableSetOf<CheckoutAdditionalCallback>()

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val additionalCallbacks: Set<CheckoutAdditionalCallback>
        get() = _additionalCallbacks

    init {
        apply(additionalCallbacksBlock)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun addAdditionalCallback(callback: CheckoutAdditionalCallback) {
        _additionalCallbacks.add(callback)
    }
}

/**
 * Returns the first registered callback that is an instance of [T], or `null` if none match.
 *
 * Registering multiple callbacks that implement the same interface type is not supported;
 * in that case this returns the first one added.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <reified T : CheckoutAdditionalCallback> Set<CheckoutAdditionalCallback>.getAdditionalCallback(): T? {
    return filterIsInstance<T>().firstOrNull()
}

interface CheckoutAdditionalCallback
