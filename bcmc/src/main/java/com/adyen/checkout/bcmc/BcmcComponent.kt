/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BcmcComponent(
    savedStateHandle: SavedStateHandle,
    private val bcmcDelegate: BcmcDelegate,
    configuration: BcmcConfiguration,
) : BasePaymentComponent<BcmcConfiguration, BcmcInputData, BcmcOutputData,
    PaymentComponentState<CardPaymentMethod>>(savedStateHandle, bcmcDelegate, configuration) {

    override val inputData: BcmcInputData = BcmcInputData()

    init {
        bcmcDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        bcmcDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)

        bcmcDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)

        bcmcDelegate.initialize(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: BcmcInputData) {
        bcmcDelegate.onInputDataChanged(inputData)
    }

    fun isCardNumberSupported(cardNumber: String?): Boolean {
        return bcmcDelegate.isCardNumberSupported(cardNumber)
    }

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<BcmcComponent, BcmcConfiguration> = BcmcComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BCMC)

        internal val SUPPORTED_CARD_TYPE = CardType.BCMC
    }
}
