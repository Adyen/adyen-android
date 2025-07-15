/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.bacs.internal.provider.BacsDirectDebitComponentProvider
import com.adyen.checkout.bacs.internal.ui.BacsDirectDebitDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.old.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.BACS] payment method.
 */
class BacsDirectDebitComponent internal constructor(
    private val bacsDelegate: BacsDirectDebitDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<BacsDirectDebitComponentState>
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        bacsDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        bacsDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<BacsDirectDebitComponentState>) -> Unit
    ) {
        bacsDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        bacsDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    /**
     * Sets the displayed BACS view as the final confirmation view.
     * Should only be called if the form is valid.
     *
     * @return whether the view was successfully changed.
     */
    fun setConfirmationMode(): Boolean {
        return (delegate as? BacsDirectDebitDelegate)?.setMode(BacsDirectDebitMode.CONFIRMATION) ?: false
    }

    /**
     * Resets the displayed BACS view back to the form input view.
     *
     * @return whether the view was successfully changed.
     */
    fun setInputMode(): Boolean {
        return (delegate as? BacsDirectDebitDelegate)?.setMode(BacsDirectDebitMode.INPUT) ?: false
    }

    /**
     * Handle back press in [BacsDirectDebitComponent] if necessary.
     *
     * @return Whether back press has been handled or not.
     */
    fun handleBackPress(): Boolean {
        return (delegate as? BacsDirectDebitDelegate)?.handleBackPress() ?: false
    }

    override fun isConfirmationRequired(): Boolean = bacsDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit()
            ?: adyenLog(AdyenLogLevel.ERROR) { "Component is currently not submittable, ignoring." }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? BacsDirectDebitDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: adyenLog(AdyenLogLevel.ERROR) { "Payment component is not interactable, ignoring." }
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        bacsDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = BacsDirectDebitComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BACS)
    }
}
