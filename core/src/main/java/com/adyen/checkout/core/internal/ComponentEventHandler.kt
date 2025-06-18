/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import kotlinx.coroutines.CoroutineScope

internal interface ComponentEventHandler<T : PaymentComponentState<*>> {

    /**
     * Do not keep a local references of this scope if you don't need to.
     *
     * If you have to keep any references to [CoroutineScope], use [onCleared] to clear them.
     */
    fun initialize(coroutineScope: CoroutineScope)

    fun onCleared()

    fun onPaymentComponentEvent(event: PaymentComponentEvent<T>)
}
