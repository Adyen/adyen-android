/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator

internal class GooglePayComponentStateValidator : ComponentStateValidator<GooglePayComponentState> {

    override fun validate(state: GooglePayComponentState): GooglePayComponentState {
        return state
    }

    override fun isValid(state: GooglePayComponentState): Boolean {
        // this validation is triggered when the component is submitted (pay button is clicked)
        // at that moment the google pay sheet has not been opened yet so the GooglePayComponentState.paymentData is
        // still null
        // so the only parameter that can be validated here is the availability check result
        return state.isAvailable
    }
}
