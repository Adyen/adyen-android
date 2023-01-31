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
import com.adyen.checkout.action.ActionHandlingComponent
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.ButtonComponent
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.mergeViewFlows
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.toActionCallback
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sepa.SepaComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class SepaComponent internal constructor(
    private val sepaDelegate: SepaDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
) : ViewModel(),
    PaymentComponent<PaymentComponentState<SepaPaymentMethod>>,
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
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<SepaPaymentMethod>>) -> Unit
    ) {
        sepaDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    override fun removeObserver() {
        sepaDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = sepaDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        sepaDelegate.onCleared()
        genericActionDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<SepaComponent, SepaConfiguration> = SepaComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.SEPA)
    }
}
