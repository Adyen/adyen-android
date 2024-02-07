/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.card.internal.data.model.LookupAddress
import com.adyen.checkout.card.internal.provider.CardComponentProvider
import com.adyen.checkout.card.internal.ui.CardDelegate
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
 * A [PaymentComponent] that supports the [PaymentMethodTypes.SCHEME] payment method.
 */
@Suppress("TooManyFunctions")
open class CardComponent constructor(
    private val cardDelegate: CardDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<CardComponentState>,
) :
    ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        cardDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        cardDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<CardComponentState>) -> Unit
    ) {
        cardDelegate.observe(lifecycleOwner, viewModelScope, callback)

        genericActionDelegate.observe(
            lifecycleOwner,
            viewModelScope,
            callback.toActionCallback(),
        )
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun removeObserver() {
        cardDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = cardDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? CardDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    /**
     * Set a callback that will be called when the bin value changes.
     *
     * @param listener The callback that will be called when the bin value changes. The bin value will be passed to it,
     * this are up to the first 6 or 8 characters of the card number.
     */
    fun setOnBinValueListener(listener: ((binValue: String) -> Unit)?) {
        cardDelegate.setOnBinValueListener(listener)
    }

    /**
     * Set a callback that will be called when a bin lookup is performed.
     *
     * @param listener The callback that will be called when a bin lookup is performed. A list of [BinLookupData] will
     * be passed, which contains information about the detected brands.
     */
    fun setOnBinLookupListener(listener: ((data: List<BinLookupData>) -> Unit)?) {
        cardDelegate.setOnBinLookupListener(listener)
    }

    // TODO address lookup
    fun setAddressLookupQueryChangedListener(listener: ((query: String) -> Unit)?) {
        cardDelegate.setAddressLookupQueryChangedListener(listener)
    }

    // TODO address lookup
    fun updateAddressLookupOptions(options: List<LookupAddress>) {
        cardDelegate.updateAddressLookupOptions(options)
    }

    fun handleBackPress(): Boolean {
        return (delegate as? CardDelegate)?.handleBackPress() ?: false
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        cardDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = CardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.SCHEME)
    }
}
