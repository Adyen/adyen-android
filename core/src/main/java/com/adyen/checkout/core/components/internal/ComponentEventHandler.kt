/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal interface ComponentEventHandler<T : PaymentComponentState<*>> {

    suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult
}
