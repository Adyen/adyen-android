/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus

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
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.paybybankus.internal.PayByBankUSDelegate
import com.adyen.checkout.paybybankus.internal.provider.PayByBankUSComponentProvider
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.old.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.PAY_BY_BANK_US] payment method.
 */
class PayByBankUSComponent internal constructor(
    private val payByBankUSDelegate: PayByBankUSDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<PayByBankUSComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        payByBankUSDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        payByBankUSDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PayByBankUSComponentState>) -> Unit
    ) {
        payByBankUSDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        payByBankUSDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? PayByBankUSDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: adyenLog(AdyenLogLevel.ERROR) { "Payment component is not interactable, ignoring." }
    }

    override fun onCleared() {
        super.onCleared()
        payByBankUSDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = PayByBankUSComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.PAY_BY_BANK_US)
    }
}
