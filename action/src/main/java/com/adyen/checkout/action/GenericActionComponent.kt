/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */
package com.adyen.checkout.action

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponent
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.ActionComponentEventHandler
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow

class GenericActionComponent internal constructor(
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: ActionHandlingComponent,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    ViewableComponent,
    IntentHandlingComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ActionDelegate
        get() = genericActionDelegate.delegate

    override val viewFlow: Flow<ComponentViewType?>
        get() = genericActionDelegate.viewFlow

    init {
        genericActionDelegate.initialize(viewModelScope)
    }

    internal fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        genericActionDelegate.removeObserver()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        genericActionDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<
            GenericActionComponent,
            GenericActionConfiguration,
            GenericActionDelegate
            > = GenericActionComponentProvider()
    }
}
