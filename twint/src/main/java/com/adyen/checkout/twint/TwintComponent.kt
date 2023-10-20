/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.internal.provider.TwintComponentProvider
import com.adyen.checkout.twint.internal.ui.TwintDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

class TwintComponent internal constructor(
    private val twintDelegate: TwintDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<TwintComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = genericActionDelegate.viewFlow

    init {
        twintDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
    ) {
        twintDelegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        twintDelegate.removeObserver()
    }

    fun startTwintScreen(activity: ComponentActivity) {
        twintDelegate.startTwintScreen(activity)
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        adyenLog(AdyenLogLevel.WARN) { "Interaction with TwintComponent can't be blocked" }
    }

    override fun onCleared() {
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        super.onCleared()
        twintDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = TwintComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.TWINT)
    }
}
