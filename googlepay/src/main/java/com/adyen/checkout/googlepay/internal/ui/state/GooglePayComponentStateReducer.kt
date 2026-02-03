/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class GooglePayComponentStateReducer : ComponentStateReducer<GooglePayComponentState, GooglePayIntent> {

    override fun reduce(state: GooglePayComponentState, intent: GooglePayIntent): GooglePayComponentState {
        return when (intent) {
            is GooglePayIntent.UpdateLoading -> state.copy(isLoading = intent.isLoading)
            is GooglePayIntent.UpdateButtonVisible -> state.copy(isButtonVisible = intent.isButtonVisible)
            is GooglePayIntent.UpdatePaymentData -> state.copy(paymentData = intent.paymentData)
            is GooglePayIntent.UpdateAvailability -> state.copy(isAvailable = intent.isAvailable)
        }
    }
}
