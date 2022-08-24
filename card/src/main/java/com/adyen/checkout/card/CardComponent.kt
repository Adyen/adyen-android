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
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class CardComponent(
    savedStateHandle: SavedStateHandle,
    private val cardDelegate: CardDelegate,
    cardConfiguration: CardConfiguration
) : BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardComponentState>(
    savedStateHandle,
    cardDelegate,
    cardConfiguration
) {

    override val inputData: CardInputData
        get() = cardDelegate.inputData

    init {
        cardDelegate.initialize(viewModelScope)

        observeOutputData()
        observeComponentState()
        observeExceptions()
    }

    private fun observeOutputData() {
        cardDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)
    }

    private fun observeComponentState() {
        cardDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    private fun observeExceptions() {
        cardDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun onInputDataChanged(inputData: CardInputData) {
        cardDelegate.onInputDataChanged(inputData)
    }

    override fun requiresInput() = cardDelegate.requiresInput()

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun onCleared() {
        super.onCleared()
        cardDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
    }
}
