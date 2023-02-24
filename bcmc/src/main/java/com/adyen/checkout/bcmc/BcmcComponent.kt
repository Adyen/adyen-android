/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.internal.ActionHandlingComponent
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.bcmc.BcmcComponent.Companion.PROVIDER
import com.adyen.checkout.bcmc.internal.provider.BcmcComponentProvider
import com.adyen.checkout.bcmc.internal.ui.BcmcDelegate
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BcmcComponent internal constructor(
    private val bcmcDelegate: BcmcDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<PaymentComponentState<CardPaymentMethod>>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        bcmcDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        bcmcDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<CardPaymentMethod>>) -> Unit
    ) {
        bcmcDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        bcmcDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = bcmcDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? BcmcDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        bcmcDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = BcmcComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BCMC)

        internal val SUPPORTED_CARD_TYPE = CardBrand(cardType = CardType.BCMC)
    }
}
