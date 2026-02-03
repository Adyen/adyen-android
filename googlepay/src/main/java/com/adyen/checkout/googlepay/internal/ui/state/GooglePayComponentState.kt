/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.google.android.gms.wallet.PaymentData

internal data class GooglePayComponentState(
    val isButtonVisible: Boolean,
    val isLoading: Boolean,
    val isAvailable: Boolean,
    val paymentData: PaymentData?,
) : ComponentState
