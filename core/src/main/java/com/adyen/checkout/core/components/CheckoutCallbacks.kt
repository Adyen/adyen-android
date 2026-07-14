/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo

/**
 * Base class for the callbacks used to interact with a [CheckoutController].
 *
 * Use one of the subclasses that matches your flow: [SessionCheckoutCallbacks], [AdvancedCheckoutCallbacks] or
 * [ActionOnlyCheckoutCallbacks].
 *
 * @param additionalCallbacksBlock An optional block to register payment-method-specific [CheckoutAdditionalCallback]s.
 */
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
    return find { it is T } as? T
}

/**
 * Marker interface for payment-method-specific callbacks that can be registered through [CheckoutCallbacks].
 */
interface CheckoutAdditionalCallback
