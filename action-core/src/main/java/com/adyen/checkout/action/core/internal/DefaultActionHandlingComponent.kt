/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action.core.internal

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionHandlingComponent(
    private val genericActionDelegate: GenericActionDelegate,
    private val paymentDelegate: PaymentComponentDelegate<*>,
) : ActionHandlingComponent {

    private var isHandlingAction: Boolean = false

    val activeDelegate: ComponentDelegate
        get() = if (isHandlingAction) {
            genericActionDelegate.delegate
        } else {
            paymentDelegate
        }

    override fun canHandleAction(action: Action): Boolean {
        return GenericActionComponent.PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        isHandlingAction = true
        genericActionDelegate.handleAction(action, activity)
    }

    override fun handleIntent(intent: Intent) {
        genericActionDelegate.handleIntent(intent)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        genericActionDelegate.setOnRedirectListener(listener)
    }
}
