/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.boleto.internal.provider.BoletoComponentProvider
import com.adyen.checkout.boleto.internal.ui.BoletoDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.BOLETOBANCARIO],
 * [PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL], [PaymentMethodTypes.BOLETOBANCARIO_BRADESCO],
 * [PaymentMethodTypes.BOLETOBANCARIO_HSBC], [PaymentMethodTypes.BOLETOBANCARIO_ITAU],
 * [PaymentMethodTypes.BOLETOBANCARIO_SANTANDER] and [PaymentMethodTypes.BOLETO_PRIMEIRO_PAY] payment methods.
 */
class BoletoComponent internal constructor(
    private val boletoDelegate: BoletoDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<BoletoComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        boletoDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        boletoDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<BoletoComponentState>) -> Unit
    ) {
        boletoDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        boletoDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = boletoDelegate.isConfirmationRequired()

    override fun submit() {
        (boletoDelegate as? ButtonDelegate)?.onSubmit()
            ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (boletoDelegate as? BoletoDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        boletoDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = BoletoComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.BOLETOBANCARIO,
            PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
            PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
            PaymentMethodTypes.BOLETOBANCARIO_HSBC,
            PaymentMethodTypes.BOLETOBANCARIO_ITAU,
            PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
            PaymentMethodTypes.BOLETO_PRIMEIRO_PAY,
        )
    }
}
