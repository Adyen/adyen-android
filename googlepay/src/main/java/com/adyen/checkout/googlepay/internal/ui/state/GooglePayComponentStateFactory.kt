/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory

internal class GooglePayComponentStateFactory : ComponentStateFactory<GooglePayComponentState> {

    override fun createInitialState() = GooglePayComponentState(
        isButtonVisible = true,
        isLoading = false,
        isAvailable = false,
        paymentData = null,
    )
}
