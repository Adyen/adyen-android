/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.ui

import com.adyen.checkout.components.PaymentComponentState

sealed class DropInActivityEvent {
    data class MakePartialPayment(val paymentComponentState: PaymentComponentState<*>) : DropInActivityEvent()
    object ShowPaymentMethods : DropInActivityEvent()
}
