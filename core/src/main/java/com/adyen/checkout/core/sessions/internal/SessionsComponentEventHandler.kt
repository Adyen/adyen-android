/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ComponentEventHandler
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.SessionsComponentCallbacks
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.SessionError

internal class SessionsComponentEventHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val componentCallbacks: SessionsComponentCallbacks,
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                // TODO - Sessions Flow. If not taken over make call
                if (componentCallbacks.onSubmit == null) {
                    makePaymentsCall(event.state)
                } else {
                    componentCallbacks.onSubmit(event.state)
                }
            }
        }
    }

    private suspend fun makePaymentsCall(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        return when (val sessionResult = sessionInteractor.submitPayment(paymentComponentState)) {
            is SessionCallResult.Payments.Action -> CheckoutResult.Action(sessionResult.action)
            is SessionCallResult.Payments.Error -> CheckoutResult.Error(
                SessionError(
                    message = sessionResult.throwable.message.orEmpty(),
                    cause = sessionResult.throwable,
                ),
            )
            // TODO - Implement Finished case
            is SessionCallResult.Payments.Finished -> CheckoutResult.Finished()
        }
    }

    override suspend fun onActionComponentEvent(event: ActionComponentEvent): CheckoutResult {
        return when (event) {
            is ActionComponentEvent.ActionDetails -> {
                if (componentCallbacks.onAdditionalDetails == null) {
                    makeDetailsCall(event.data)
                } else {
                    componentCallbacks.onAdditionalDetails(event.data)
                }
            }

            is ActionComponentEvent.Error -> {
                componentCallbacks.onError(event.error)
                CheckoutResult.Error(event.error)
            }
        }
    }

    private suspend fun makeDetailsCall(actionComponentData: ActionComponentData): CheckoutResult {
        return when (val sessionResult = sessionInteractor.submitDetails(actionComponentData)) {
            is SessionCallResult.Details.Action -> CheckoutResult.Action(sessionResult.action)
            is SessionCallResult.Details.Error -> CheckoutResult.Error(
                SessionError(
                    message = sessionResult.throwable.message.orEmpty(),
                    cause = sessionResult.throwable,
                ),
            )
            // TODO - Propagate session result to CheckoutResult.Finished once its data class is updated.
            is SessionCallResult.Details.Finished -> CheckoutResult.Finished()
        }
    }
}
