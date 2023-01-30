/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.ActionHandlingComponent
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.blik.BlikComponent.Companion.PROVIDER
import com.adyen.checkout.components.ButtonComponent
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.extensions.mergeViewFlows
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.toActionCallback
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.provider.SessionStoredPaymentComponentProvider
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BlikComponent internal constructor(
    private val blikDelegate: BlikDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<PaymentComponentState<BlikPaymentMethod>>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        blikDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        blikDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<BlikPaymentMethod>>) -> Unit
    ) {
        blikDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        blikDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = blikDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? BlikDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        blikDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: SessionStoredPaymentComponentProvider<
            BlikComponent,
            BlikConfiguration,
            PaymentComponentState<BlikPaymentMethod>> = BlikComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BLIK)
    }
}
