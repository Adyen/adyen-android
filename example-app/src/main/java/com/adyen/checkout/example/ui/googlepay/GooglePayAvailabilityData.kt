/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.GooglePayConfiguration

internal data class GooglePayAvailabilityData(
    val paymentMethod: PaymentMethod,
    val googlePayConfiguration: GooglePayConfiguration,
    val callback: ComponentAvailableCallback
)
