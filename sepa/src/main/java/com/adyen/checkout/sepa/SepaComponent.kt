/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.internal.ActionHandlingComponent
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.SepaPaymentMethod
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.sepa.SepaComponent.Companion.PROVIDER
import com.adyen.checkout.sepa.internal.provider.SepaComponentProvider
import com.adyen.checkout.sepa.internal.ui.SepaDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class SepaComponent internal constructor(
    private val sepaDelegate: SepaDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<PaymentComponentState<SepaPaymentMethod>>
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        sepaDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        sepaDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<SepaPaymentMethod>>) -> Unit
    ) {
        sepaDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        sepaDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = sepaDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? SepaDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        sepaDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = SepaComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.SEPA)
    }
}
