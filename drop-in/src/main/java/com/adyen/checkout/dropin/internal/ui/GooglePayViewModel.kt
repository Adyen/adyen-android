/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/1/2022.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class GooglePayViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {

    private val eventChannel: Channel<GooglePayFragmentEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    private var isGooglePayStarted: Boolean? by SavedStateHandleProperty(IS_GOOGLE_PAY_STARTED)

    fun fragmentLoaded() {
        if (isGooglePayStarted == true) return
        isGooglePayStarted = true
        viewModelScope.launch {
            adyenLog(AdyenLogLevel.DEBUG) { "Sending start GooglePay event" }
            eventChannel.send(GooglePayFragmentEvent.StartGooglePay)
        }
    }

    companion object {
        private const val IS_GOOGLE_PAY_STARTED = "IS_GOOGLE_PAY_STARTED"
    }
}

internal sealed class GooglePayFragmentEvent {
    object StartGooglePay : GooglePayFragmentEvent()
}
