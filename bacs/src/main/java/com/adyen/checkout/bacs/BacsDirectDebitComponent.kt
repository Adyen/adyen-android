/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.bacs.BacsDirectDebitComponent.Companion.PROVIDER
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BacsDirectDebitComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    override val delegate: BacsDirectDebitDelegate,
) : ViewModel(),
    PaymentComponent<BacsDirectDebitComponentState>,
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<BacsDirectDebitComponentState>) -> Unit
    ) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    override fun removeObserver() {
        delegate.removeObserver()
    }

    /**
     * Sets the displayed BACS view as the final confirmation view.
     * Should only be called if the form is valid.
     *
     * @return whether the view was successfully changed.
     */
    fun setConfirmationMode(): Boolean {
        return delegate.setMode(BacsDirectDebitMode.CONFIRMATION)
    }

    /**
     * Resets the displayed BACS view back to the form input view.
     *
     * @return whether the view was successfully changed.
     */
    fun setInputMode(): Boolean {
        return delegate.setMode(BacsDirectDebitMode.INPUT)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<BacsDirectDebitComponent, BacsDirectDebitConfiguration> =
            BacsDirectDebitComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BACS)
    }
}
