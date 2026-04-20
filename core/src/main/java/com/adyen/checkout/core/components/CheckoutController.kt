/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.content.Context
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.internal.CheckoutControllerFactory
import com.adyen.checkout.core.components.internal.CheckoutFlow
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope

fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Advanced,
    callbacks: AdvancedCheckoutCallbacks,
    // TODO - find a way to not require application context in the controller
    applicationContext: Context,
    coroutineScope: CoroutineScope,
): CheckoutController {
    return CheckoutControllerFactory().create(
        target = target,
        context = context,
        callbacks = callbacks,
        applicationContext = applicationContext,
        coroutineScope = coroutineScope,
    )
}

fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    // TODO - find a way to not require application context in the controller
    applicationContext: Context,
    coroutineScope: CoroutineScope,
): CheckoutController {
    return CheckoutControllerFactory().create(
        target = target,
        context = context,
        callbacks = callbacks,
        applicationContext = applicationContext,
        coroutineScope = coroutineScope,
    )
}

class CheckoutController internal constructor(
    private val flow: CheckoutFlow,
) {

    internal val paymentComponent: PaymentComponent<*>? get() = flow.paymentComponent

    internal val actionComponent: ActionComponent? get() = flow.actionComponent

    internal var onNavigate: ((CheckoutRoute) -> Unit)?
        get() = flow.onNavigate
        set(value) {
            flow.onNavigate = value
        }

    fun submit() {
        flow.submit()
    }
}
