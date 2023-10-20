/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.action.Action

internal sealed class GooglePayEvent {

    data class CheckAvailability(
        val paymentMethod: PaymentMethod,
        val callback: ComponentAvailableCallback
    ) : GooglePayEvent()

    data class PaymentResult(val result: String) : GooglePayEvent()

    data class AdditionalAction(val action: Action) : GooglePayEvent()
}
