/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.googlepay.GooglePayButtonStyling

internal data class GooglePayButtonViewState(
    val allowedPaymentMethods: String,
    val buttonStyling: GooglePayButtonStyling?,
    val isLoading: Boolean,
)
