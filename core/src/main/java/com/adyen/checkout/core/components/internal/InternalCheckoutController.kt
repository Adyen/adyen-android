/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/5/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.IntegrationType
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun InternalCheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Advanced,
    callbacks: AdvancedCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController {
    return CheckoutControllerFactory().create(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
        integrationType = IntegrationType.DROP_IN,
    )
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun InternalCheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController {
    return CheckoutControllerFactory().create(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
        integrationType = IntegrationType.DROP_IN,
    )
}
