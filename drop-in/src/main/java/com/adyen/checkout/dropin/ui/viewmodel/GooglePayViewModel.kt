/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/1/2022.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.bundle.SavedStateHandleContainer
import com.adyen.checkout.components.bundle.SavedStateHandleProperty
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class GooglePayViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {
    companion object {
        private val TAG = LogUtil.getTag()
        private const val IS_GOOGLE_PAY_STARTED = "IS_GOOGLE_PAY_STARTED"
    }

    private val eventChannel: Channel<GooglePayFragmentEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    private var isGooglePayStarted: Boolean? by SavedStateHandleProperty(IS_GOOGLE_PAY_STARTED)

    fun fragmentLoaded() {
        if (isGooglePayStarted == true) return
        isGooglePayStarted = true
        viewModelScope.launch {
            Logger.d(TAG, "Sending start GooglePay event")
            eventChannel.send(GooglePayFragmentEvent.StartGooglePay)
        }
    }
}

internal sealed class GooglePayFragmentEvent {
    object StartGooglePay : GooglePayFragmentEvent()
}
