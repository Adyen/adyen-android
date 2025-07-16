/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.await.old

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.await.old.internal.provider.AwaitComponentProvider
import com.adyen.checkout.await.old.internal.ui.AwaitDelegate
import com.adyen.checkout.components.core.RedirectableActionComponent
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * An [ActionComponent] that is able to handle the 'await' action.
 */
class AwaitComponent internal constructor(
    override val delegate: AwaitDelegate,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    ViewableComponent,
    RedirectableActionComponent {

    override val viewFlow: Flow<ComponentViewType?> get() = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    internal fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        delegate.removeObserver()
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
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
        val PROVIDER: ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> =
            AwaitComponentProvider()
    }
}
