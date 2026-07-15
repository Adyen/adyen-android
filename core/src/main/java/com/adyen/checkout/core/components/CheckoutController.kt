/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.content.Intent
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.CheckoutControllerFactory
import com.adyen.checkout.core.components.internal.CheckoutFlow
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Creates a [CheckoutController] for the advanced flow.
 *
 * @param target The [CheckoutTarget] indicating which payment method (or stored payment method) to use.
 * @param context The [CheckoutContext.Advanced] obtained from [Checkout.setup].
 * @param callbacks The [AdvancedCheckoutCallbacks] used to handle the payment flow.
 * @param coroutineScope The [CoroutineScope] tied to the lifecycle that owns this controller.
 * @return A [CheckoutController] instance.
 */
fun CheckoutController(
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
    )
}

/**
 * Creates a [CheckoutController] for the sessions flow.
 *
 * @param target The [CheckoutTarget] indicating which payment method (or stored payment method) to use.
 * @param context The [CheckoutContext.Sessions] obtained from [Checkout.setup].
 * @param callbacks The [SessionCheckoutCallbacks] used to observe the payment flow.
 * @param coroutineScope The [CoroutineScope] tied to the lifecycle that owns this controller.
 * @return A [CheckoutController] instance.
 */
fun CheckoutController(
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
    )
}

/**
 * Creates a [CheckoutController] for the action-only flow.
 *
 * @param context The [CheckoutContext.ActionOnly] obtained from [Checkout.setup].
 * @param callbacks The [ActionOnlyCheckoutCallbacks] used to handle the action flow.
 * @param coroutineScope The [CoroutineScope] tied to the lifecycle that owns this controller.
 * @return A [CheckoutController] instance.
 */
fun CheckoutController(
    context: CheckoutContext.ActionOnly,
    callbacks: ActionOnlyCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController {
    return CheckoutControllerFactory().create(
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
    )
}

/**
 * Controls a checkout flow.
 *
 * Create an instance using one of the [CheckoutController] factory functions, then drive the flow through
 * [submit] and [handleReturn], and observe navigation changes if necessary through [navigation].
 */
class CheckoutController internal constructor(
    private val flow: CheckoutFlow,
    internal val environment: Environment,
    internal val shopperLocale: Locale,
) {

    internal val paymentComponent: PaymentComponent? get() = flow.paymentComponent

    internal val actionComponent: ActionComponent? get() = flow.actionComponent

    /**
     * A [Flow] of [CheckoutRoute] events that indicate which screen should be displayed.
     *
     * Collect this flow to observe navigation changes and update the UI accordingly (e.g. showing
     * the payment method input, an action screen, or secondary content).
     */
    val navigation: Flow<CheckoutRoute> get() = flow.navigation

    /**
     * Submits the current payment data.
     *
     * After calling this method, the input data is validated. If validation fails, the corresponding errors
     * are displayed in the UI and the submission is aborted.
     *
     * If [requiresUserInteraction] returns `false`, this can be called directly without waiting for
     * user input.
     */
    fun submit() {
        flow.submit()
    }

    /**
     * Indicates whether the payment method requires user interaction before submitting.
     *
     * When this returns `false`, no UI needs to be rendered and [submit] can be called directly
     * without requiring a user action (e.g. a button click).
     *
     * When this returns `true`, the payment method UI should be displayed so the user can provide
     * the required input before calling [submit].
     *
     * @return `true` if user interaction is needed, `false` otherwise.
     */
    fun requiresUserInteraction(): Boolean = flow.requiresUserInteraction()

    /**
     * Handles the return from an external redirect (e.g. a browser or third-party app).
     *
     * Call this method when the app receives a deep link or new intent after the user is redirected
     * externally as part of an action.
     *
     * @param intent The [Intent] received by the app upon returning from the external redirect.
     */
    fun handleReturn(intent: Intent) {
        flow.handleReturn(intent)
    }
}
