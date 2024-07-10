/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.internal.provider.TwintActionComponentProvider
import com.adyen.checkout.twint.internal.ui.TwintActionDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

class TwintActionComponent internal constructor(
    override val delegate: TwintActionDelegate,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    ViewableComponent {

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

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
    }

    override fun onCleared() {
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        super.onCleared()
        delegate.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = TwintActionComponentProvider()
    }
}
