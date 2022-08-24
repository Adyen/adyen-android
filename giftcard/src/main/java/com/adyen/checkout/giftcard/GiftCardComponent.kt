/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GiftCardComponent(
    savedStateHandle: SavedStateHandle,
    private val giftCardDelegate: GiftCardDelegate,
    configuration: GiftCardConfiguration,
) : BasePaymentComponent<GiftCardConfiguration, GiftCardInputData, GiftCardOutputData, GiftCardComponentState>(
    savedStateHandle,
    giftCardDelegate,
    configuration
) {

    override val inputData: GiftCardInputData = GiftCardInputData()

    init {
        giftCardDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        giftCardDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)

        giftCardDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)

        giftCardDelegate.initialize(viewModelScope)
    }

    override fun onInputDataChanged(inputData: GiftCardInputData) {
        giftCardDelegate.onInputDataChanged(inputData)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> = GiftCardComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)
    }
}
