/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/4/2023.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.giftcard.GiftCardComponentState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class GiftCardViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {

    private var componentState: GiftCardComponentState? by SavedStateHandleProperty(COMPONENT_STATE)

    private val eventChannel: Channel<GiftCardFragmentEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    fun onState(state: GiftCardComponentState) {
        if (state.isValid) {
            componentState = state
        }
    }

    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails) {
        val currentState = componentState
        if (currentState != null) {
            viewModelScope.launch {
                val stateWithPaymentMethod = currentState.copy(
                    data = currentState.data.copy(
                        paymentMethod = paymentMethodDetails as GiftCardPaymentMethod
                    )
                )
                Logger.d(TAG, "Sending check balance event")
                eventChannel.send(
                    GiftCardFragmentEvent.CheckBalance(stateWithPaymentMethod)
                )
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val COMPONENT_STATE = "COMPONENT_STATE"
    }
}

internal sealed class GiftCardFragmentEvent {
    data class CheckBalance(val paymentComponentState: GiftCardComponentState) : GiftCardFragmentEvent()
}
