/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */
package com.adyen.checkout.threeds2.old

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.RedirectableActionComponent
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.components.core.internal.IntentHandlingComponent
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.threeds2.old.internal.provider.Adyen3DS2ComponentProvider
import com.adyen.checkout.threeds2.old.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * An [ActionComponent] that is able to handle 3DS2 related actions.
 */
class Adyen3DS2Component internal constructor(
    override val delegate: Adyen3DS2Delegate,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    IntentHandlingComponent,
    ViewableComponent,
    RedirectableActionComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    internal fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        delegate.removeObserver()
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
    }

    /**
     * Call this method when receiving the return URL from the 3DS redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        delegate.handleIntent(intent)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        delegate.setOnRedirectListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        delegate.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER: ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration, Adyen3DS2Delegate> =
            Adyen3DS2ComponentProvider()
    }
}
