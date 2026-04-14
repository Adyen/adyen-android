/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.internal.ui.PaymentComponent

internal class ActionOnlyCheckoutFlow(
    action: Action,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    override val paymentComponent: PaymentComponent<*>? = null

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    override var onNavigate: ((CheckoutRoute) -> Unit)? = null

    init {
        actionHandler.handleAction(action)
    }

    override fun submit() {
        // No-op: action-only flow does not support submit
    }
}
