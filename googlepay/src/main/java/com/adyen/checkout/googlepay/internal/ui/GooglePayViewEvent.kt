/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData

internal sealed class GooglePayViewEvent {
    data class LaunchGooglePay(val task: Task<PaymentData>) : GooglePayViewEvent()
}
