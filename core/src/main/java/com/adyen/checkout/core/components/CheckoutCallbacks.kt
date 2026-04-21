/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class CheckoutCallbacks(
    additionalCallbacksBlock: CheckoutCallbacks.() -> Unit,
) {

    private val additionalCallbacks = mutableMapOf<KClass<out CheckoutAdditionalCallback>, CheckoutAdditionalCallback>()

    init {
        apply(additionalCallbacksBlock)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : CheckoutAdditionalCallback> addAdditionalCallback(callback: T, clazz: KClass<T>) {
        additionalCallbacks[clazz] = callback
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : CheckoutAdditionalCallback> getAdditionalCallback(clazz: KClass<T>): T? {
        return additionalCallbacks[clazz]?.let { clazz.safeCast(it) }
    }
}

interface CheckoutAdditionalCallback
