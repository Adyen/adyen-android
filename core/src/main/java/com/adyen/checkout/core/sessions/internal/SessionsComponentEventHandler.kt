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

internal class SessionsComponentEventHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val checkoutCallback: CheckoutCallback?,
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                // TODO - Sessions Flow. If not taken over make call
                if (checkoutCallback?.beforeSubmit(event.state) != true) {
                    makePaymentsCall(event.state)
                } else {
                    checkoutCallback.onSubmit(event.state)
                }
            }
        }
    }

    private suspend fun makePaymentsCall(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return when (val sessionResult = sessionInteractor.submitPayment(paymentComponentState)) {
            is SessionCallResult.Payments.Action -> CheckoutResult.Action(sessionResult.action)
            // TODO - Implement Error case
            is SessionCallResult.Payments.Error -> CheckoutResult.Error()
            // TODO - Implement Finished case
            is SessionCallResult.Payments.Finished -> CheckoutResult.Finished()
        }
    }
}
