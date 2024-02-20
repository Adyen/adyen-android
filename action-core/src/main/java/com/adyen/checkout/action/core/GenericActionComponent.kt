/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */
package com.adyen.checkout.action.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.components.core.internal.IntentHandlingComponent
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * An [ActionComponent] that is able to handle every action.
 */
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
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        genericActionDelegate.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER: ActionComponentProvider<
            GenericActionComponent,
            GenericActionConfiguration,
            GenericActionDelegate,
            > = GenericActionComponentProvider()
    }
}
