package com.adyen.checkout.components.ui

import com.adyen.checkout.components.channel.bufferedChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class DefaultButtonDelegate : ButtonDelegate {

    private val _submitChannel: Channel<Unit> = bufferedChannel()
    override val submitFlow: Flow<Unit> = _submitChannel.receiveAsFlow()

    override fun onSubmit() {
        _submitChannel.trySend(Unit)
    }
}
