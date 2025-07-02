/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core

import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

interface CheckoutCallback {

    fun beforeSubmit(paymentComponentState: PaymentComponentState<*>): Boolean = false

    fun onSubmit(
        paymentComponentState: PaymentComponentState<*>,
        onCompletion: (CheckoutResult) -> Unit
    )
}
