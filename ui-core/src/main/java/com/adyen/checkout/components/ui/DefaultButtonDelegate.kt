package com.adyen.checkout.components.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class DefaultButtonDelegate : ButtonDelegate {

    private val _submitChannel: Channel<PaymentComponentState<out PaymentMethodDetails>> = bufferedChannel()
    override val submitFlow: Flow<PaymentComponentState<out PaymentMethodDetails>> = _submitChannel.receiveAsFlow()

    override fun onSubmit() {

    }
}
