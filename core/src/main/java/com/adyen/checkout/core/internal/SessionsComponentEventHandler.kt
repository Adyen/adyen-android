/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.CheckoutResult
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SessionsComponentEventHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val checkoutCallback: CheckoutCallback?,
) : ComponentEventHandler<T> {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                // TODO - Sessions Flow. If not taken over make call
                when {
                    checkoutCallback == null || !checkoutCallback.beforeSubmit(event.state) -> {
                        makePaymentsCall(event.state)
                    }
                    else -> {
                        checkoutCallback.onSubmit(event.state) { checkoutResult ->
                            handleResult(checkoutResult)
                        }
                    }
                }
            }
        }
    }

    private fun handleResult(checkoutResult: CheckoutResult) {
        when (checkoutResult) {
            is CheckoutResult.Action -> {
                // TODO - Handle Action
            }
            is CheckoutResult.Error -> {
                // TODO - Handle Error
            }
            is CheckoutResult.Finished -> {
                // TODO - Handle Finished
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
