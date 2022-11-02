/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.bcmc.BcmcComponent.Companion.PROVIDER
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.flow.mapToCallbackWithLifeCycle
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BcmcComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: BcmcDelegate,
    configuration: BcmcConfiguration,
) : BasePaymentComponent<BcmcConfiguration,
    PaymentComponentState<CardPaymentMethod>>(savedStateHandle, delegate, configuration),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    private var observerJobs: MutableList<Job> = mutableListOf()

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<CardPaymentMethod>>) -> Unit
    ) {
        removeObserver()
        delegate.componentStateFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(PaymentComponentEvent.StateChanged(it))
        }

        delegate.exceptionFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(PaymentComponentEvent.Error(ComponentError(it)))
        }
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        removeObserver()
    }

    override fun removeObserver() {
        if (observerJobs.isEmpty()) return
        Logger.d(TAG, "cleaning up existing observer")
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<BcmcComponent, BcmcConfiguration> = BcmcComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BCMC)

        internal val SUPPORTED_CARD_TYPE = CardType.BCMC
    }
}
