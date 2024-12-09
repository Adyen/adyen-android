/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/10/2024.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.google.android.gms.wallet.PaymentData

internal data class GooglePayOutputData(
    val isButtonVisible: Boolean,
    val isLoading: Boolean,
    val paymentData: PaymentData?,
)
