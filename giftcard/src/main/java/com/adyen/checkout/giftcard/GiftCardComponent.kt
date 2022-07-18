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
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param configuration [GiftCardConfiguration]
 */
class GiftCardComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    private val giftCardDelegate: GiftCardDelegate,
    configuration: GiftCardConfiguration,
) : BasePaymentComponent<GiftCardConfiguration, GiftCardInputData, GiftCardOutputData, GiftCardComponentState>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    override var inputData: GiftCardInputData = GiftCardInputData()

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

        viewModelScope.launch {
            giftCardDelegate.fetchPublicKey()
        }
    }

    override fun onInputDataChanged(inputData: GiftCardInputData) {
        giftCardDelegate.onInputDataChanged(inputData)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> = GiftCardComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)
    }
}
