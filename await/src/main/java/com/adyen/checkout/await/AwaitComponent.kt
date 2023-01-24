/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.await

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponent
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.ActionComponentEventHandler
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow

class AwaitComponent internal constructor(
    override val delegate: AwaitDelegate,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    ViewableComponent {

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

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> =
            AwaitComponentProvider()
    }
}
