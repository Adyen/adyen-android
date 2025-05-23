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

internal class DefaultComponentEventHandler<T : PaymentComponentState<*>>(
    val sessionInteractor: SessionInteractor
) : ComponentEventHandler<T> {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, checkoutCallback: CheckoutCallback) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                // submit
                checkoutCallback.onSubmit(event.state) { result ->
                    when (result) {
                        is CheckoutResult.Advanced -> {
                            when (result) {
                                is CheckoutResult.Advanced.Action -> {
                                    // TODO - Advanced Flow
                                }
                                is CheckoutResult.Advanced.Error -> {
                                    // TODO - Advanced Flow
                                }
                                is CheckoutResult.Advanced.Finished -> {
                                    // TODO - Advanced Flow
                                }
                            }
                        }
                        is CheckoutResult.Sessions -> {
                            //
                            makePaymentsCall(event.state)
                        }
                    }
                }
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
