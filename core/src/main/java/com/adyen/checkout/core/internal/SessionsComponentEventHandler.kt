/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SessionsComponentEventHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
) : ComponentEventHandler<T> {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, checkoutCallback: CheckoutCallback) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                // TODO - Sessions Flow. If not taken over make call
                makePaymentsCall(event.state)
            }
        }
    }

    private fun makePaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        coroutineScope.launch {
            sessionInteractor.submitPayment(paymentComponentState)
        }
    }

    override fun onCleared() {
        _coroutineScope = null
    }
}
