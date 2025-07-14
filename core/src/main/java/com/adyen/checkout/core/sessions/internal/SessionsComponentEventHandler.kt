/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.components.CheckoutCallback
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ComponentEventHandler
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
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

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, onCheckoutResult: (CheckoutResult) -> Unit) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                // TODO - Sessions Flow. If not taken over make call
                when {
                    checkoutCallback == null || !checkoutCallback.beforeSubmit(event.state) -> {
                        makePaymentsCall(event.state, onCheckoutResult)
                    }

                    else -> {
                        checkoutCallback.onSubmit(event.state) { checkoutResult ->
                            onCheckoutResult(checkoutResult)
                        }
                    }
                }
            }
        }
    }

    private fun makePaymentsCall(
        paymentComponentState: PaymentComponentState<*>,
        onCheckoutResult: (CheckoutResult) -> Unit
    ) {
        coroutineScope.launch {
            val sessionResult = sessionInteractor.submitPayment(paymentComponentState)
            val checkoutResult = when (sessionResult) {
                is SessionCallResult.Payments.Action -> CheckoutResult.Action(sessionResult.action)
                // TODO - Implement Error case
                is SessionCallResult.Payments.Error -> CheckoutResult.Error()
                // TODO - Implement Finished case
                is SessionCallResult.Payments.Finished -> CheckoutResult.Finished()
            }

            onCheckoutResult(checkoutResult)
        }
    }

    override fun onCleared() {
        _coroutineScope = null
    }
}
