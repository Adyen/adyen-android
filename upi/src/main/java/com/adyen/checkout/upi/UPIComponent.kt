/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2023.
 */

package com.adyen.checkout.upi

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.old.internal.util.mergeViewFlows
import com.adyen.checkout.upi.internal.provider.UPIComponentProvider
import com.adyen.checkout.upi.internal.ui.UPIDelegate
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.UPI], [PaymentMethodTypes.UPI_COLLECT] and
 * [PaymentMethodTypes.UPI_QR] payment methods.
 */
class UPIComponent internal constructor(
    private val upiDelegate: UPIDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<UPIComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        upiDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        upiDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<UPIComponentState>) -> Unit
    ) {
        upiDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        upiDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = upiDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit()
            ?: adyenLog(AdyenLogLevel.ERROR) { "Component is currently not submittable, ignoring." }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? UPIDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: adyenLog(AdyenLogLevel.ERROR) { "Payment component is not interactable, ignoring." }
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        upiDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        @JvmField
        val PROVIDER = UPIComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.UPI,
            PaymentMethodTypes.UPI_COLLECT,
            PaymentMethodTypes.UPI_QR,
            PaymentMethodTypes.UPI_INTENT
        )
    }
}
