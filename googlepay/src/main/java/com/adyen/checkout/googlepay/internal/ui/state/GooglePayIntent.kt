/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent
import com.google.android.gms.wallet.PaymentData

internal sealed interface GooglePayIntent : ComponentStateIntent {

    data class UpdateLoading(val isLoading: Boolean) : GooglePayIntent

    data class UpdateButtonVisible(val isButtonVisible: Boolean) : GooglePayIntent

    data class UpdatePaymentData(val paymentData: PaymentData?) : GooglePayIntent

    data class UpdateAvailability(val isAvailable: Boolean) : GooglePayIntent
}
