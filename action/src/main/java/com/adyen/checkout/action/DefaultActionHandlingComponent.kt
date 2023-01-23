/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.authentication.AuthenticationLauncher
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.threeds2.customization.UiCustomization

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionHandlingComponent(
    private val savedStateHandle: SavedStateHandle,
    private val genericActionDelegate: GenericActionDelegate,
    paymentDelegate: PaymentComponentDelegate<*>?,
) : ActionHandlingComponent {

    var activeDelegate: ComponentDelegate = paymentDelegate ?: genericActionDelegate
        private set

    private var isActionHandled: Boolean
        get() = savedStateHandle[IS_ACTION_HANDLED] ?: false
        set(value) {
            savedStateHandle[IS_ACTION_HANDLED] = value
        }

    init {
        // Restoring the state after process kill
        if (isActionHandled) {
            activeDelegate = genericActionDelegate
        }
    }

    override fun canHandleAction(action: Action): Boolean {
        return GenericActionComponent.PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        isActionHandled = true
        activeDelegate = genericActionDelegate
        genericActionDelegate.handleAction(action, activity)
        // genericActionDelegate.delegate is set when calling genericActionDelegate.handleAction, so we set the more
        // specific delegate here as soon as we can.
        activeDelegate = genericActionDelegate.delegate
    }

    override fun handleIntent(intent: Intent) {
        genericActionDelegate.handleIntent(intent)
    }

    override fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        genericActionDelegate.set3DS2UICustomization(uiCustomization)
    }

    override fun initDelegatedAuthentication(authenticationLauncher: AuthenticationLauncher) {
        genericActionDelegate.initDelegatedAuthentication(authenticationLauncher)
    }

    companion object {
        private const val IS_ACTION_HANDLED = "dahc_is_action_handled"
    }
}
