/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action.internal

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.GenericActionComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.action.Action

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionHandlingComponent(
    private val genericActionDelegate: GenericActionDelegate,
    paymentDelegate: PaymentComponentDelegate<*>?,
) : ActionHandlingComponent {

    var activeDelegate: ComponentDelegate = paymentDelegate ?: genericActionDelegate
        private set

    override fun canHandleAction(action: Action): Boolean {
        return GenericActionComponent.PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        activeDelegate = genericActionDelegate
        genericActionDelegate.handleAction(action, activity)
        // genericActionDelegate.delegate is set when calling genericActionDelegate.handleAction, so we set the more
        // specific delegate here as soon as we can.
        activeDelegate = genericActionDelegate.delegate
    }

    override fun handleIntent(intent: Intent) {
        genericActionDelegate.handleIntent(intent)
    }
}
