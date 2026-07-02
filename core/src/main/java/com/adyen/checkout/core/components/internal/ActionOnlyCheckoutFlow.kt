/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class ActionOnlyCheckoutFlow(
    action: Action,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    override val paymentComponent: PaymentComponent? = null

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    override val navigation: Flow<CheckoutRoute> = emptyFlow()

    init {
        actionHandler.handleAction(action)
    }

    override fun submit() {
        adyenLog(AdyenLogLevel.WARN) { "submit() is not supported for action-only flows" }
    }

    // Action only flows do not require user interaction
    override fun requiresUserInteraction(): Boolean = false

    override fun handleReturn(intent: Intent) {
        actionHandler.handleReturn(intent)
    }
}
