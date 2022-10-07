/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponent.Companion.PROVIDER
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class CardComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: CardDelegate,
    cardConfiguration: CardConfiguration
) :
    BasePaymentComponent<CardConfiguration, CardComponentState>(
        savedStateHandle,
        delegate,
        cardConfiguration
    ),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> get() = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)

        observeComponentState()
        observeExceptions()
    }

    private fun observeComponentState() {
        delegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    private fun observeExceptions() {
        delegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun requiresInput() = delegate.requiresInput()

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onCleared() {
        super.onCleared()
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
    }
}
