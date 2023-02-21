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
import com.adyen.checkout.action.internal.ActionHandlingComponent
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.bacs.BacsDirectDebitComponent.Companion.PROVIDER
import com.adyen.checkout.bacs.internal.provider.BacsDirectDebitComponentProvider
import com.adyen.checkout.bacs.internal.ui.BacsDirectDebitDelegate
import com.adyen.checkout.components.ButtonComponent
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.toActionCallback
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
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
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? BacsDirectDebitDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        bacsDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = BacsDirectDebitComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BACS)
    }
}
