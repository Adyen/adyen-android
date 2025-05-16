/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.paymentmethod.PaymentComponentState

internal interface ComponentEventHandler<T : PaymentComponentState<*>> {

    fun onPaymentComponentEvent(event: PaymentComponentEvent<T>)
}
