/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/1/2022.
 */

package com.adyen.checkout.dropin.old.internal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

internal class GooglePayViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {

    private val eventChannel: Channel<GooglePayFragmentEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    private var isGooglePayStarted: Boolean? by SavedStateHandleProperty(IS_GOOGLE_PAY_STARTED)

    fun onFragmentLoaded() {
        adyenLog(AdyenLogLevel.DEBUG) { "onFragmentLoaded" }
        if (isGooglePayStarted == true) return
        isGooglePayStarted = true
        eventChannel.trySend(GooglePayFragmentEvent.StartGooglePay)
    }

    companion object {
        private const val IS_GOOGLE_PAY_STARTED = "IS_GOOGLE_PAY_STARTED"
    }
}

internal abstract class GooglePayFragmentEvent {
    data object StartGooglePay : GooglePayFragmentEvent()
}
