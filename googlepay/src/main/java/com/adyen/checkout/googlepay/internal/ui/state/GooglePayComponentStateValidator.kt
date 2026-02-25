/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils

internal class GooglePayComponentStateValidator : ComponentStateValidator<GooglePayComponentState> {

    override fun validate(state: GooglePayComponentState): GooglePayComponentState {
        return state
    }

    override fun isValid(state: GooglePayComponentState): Boolean {
        val paymentData = state.paymentData ?: return false
        return runCatching { GooglePayUtils.findToken(paymentData) }
            .getOrNull()?.isNotEmpty() == true
    }
}
