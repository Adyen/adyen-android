/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/1/2023.
 */

package com.adyen.checkout.sessions

import android.util.Log
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.interactor.SessionCallResult
import com.adyen.checkout.sessions.interactor.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("TooManyFunctions")
class SessionHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val coroutineScope: CoroutineScope,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
    private val sessionComponentCallback: SessionComponentCallback<T>
) {

    init {
        coroutineScope.launch {
            sessionInteractor.sessionFlow
                .mapNotNull { it.sessionData }
                .collect { updateSessionData(it) }
        }
    }

    fun onPaymentComponentEvent(event: PaymentComponentEvent<T>) {
        Logger.v(TAG, "Event received $event")
        when (event) {
            is PaymentComponentEvent.ActionDetails -> onDetailsCallRequested(event.data)
            is PaymentComponentEvent.Error -> onComponentError(event.error)
            is PaymentComponentEvent.StateChanged -> onState(event.state)
            is PaymentComponentEvent.Submit -> onPaymentsCallRequested(event.state)
        }
    }

    private fun updateSessionData(sessionData: String) {
        Logger.v(TAG, "Updating session data - $sessionData")
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }

    private fun onPaymentsCallRequested(
        paymentComponentState: T,
    ) {
        coroutineScope.launchWithLoadingState {
            val result = sessionInteractor.onPaymentsCallRequested(
                paymentComponentState,
                sessionComponentCallback::onSubmit,
                sessionComponentCallback::onSubmit.name,
            )

            when (result) {
                is SessionCallResult.Payments.Action -> {
                    sessionComponentCallback.onAction(result.action)
                }
                is SessionCallResult.Payments.Error -> onSessionError(result.throwable)
                is SessionCallResult.Payments.Finished -> onFinished(result.resultCode)
                is SessionCallResult.Payments.NotFullyPaidOrder -> {
                    // TODO sessions: handle with gift card
                }
                is SessionCallResult.Payments.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun onDetailsCallRequested(
        actionComponentData: ActionComponentData
    ) {
        coroutineScope.launchWithLoadingState {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                sessionComponentCallback::onAdditionalDetails,
                sessionComponentCallback::onAdditionalDetails.name
            )

            when (result) {
                is SessionCallResult.Details.Action -> {
                    sessionComponentCallback.onAction(result.action)
                }
                is SessionCallResult.Details.Error -> onSessionError(result.throwable)
                is SessionCallResult.Details.Finished -> onFinished(result.resultCode)
                SessionCallResult.Details.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        coroutineScope.launchWithLoadingState {
            val result = sessionInteractor.checkBalance(
                paymentMethodData,
                sessionComponentCallback::onBalanceCheck,
                sessionComponentCallback::onBalanceCheck.name,
            )
            when (result) {
                is SessionCallResult.Balance.Error -> onSessionError(result.throwable)
                is SessionCallResult.Balance.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.Balance.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun createOrder() {
        coroutineScope.launchWithLoadingState {
            val result = sessionInteractor.createOrder(
                sessionComponentCallback::onOrderRequest,
                sessionComponentCallback::onOrderRequest.name
            )

            when (result) {
                is SessionCallResult.CreateOrder.Error -> onSessionError(result.throwable)
                is SessionCallResult.CreateOrder.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.CreateOrder.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun cancelOrder(order: OrderRequest) {
        coroutineScope.launchWithLoadingState {
            val result = sessionInteractor.cancelOrder(
                order,
                sessionComponentCallback::onOrderCancel,
                sessionComponentCallback::onOrderCancel.name
            )

            when (result) {
                is SessionCallResult.CancelOrder.Error -> onSessionError(result.throwable)
                SessionCallResult.CancelOrder.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.CancelOrder.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun CoroutineScope.launchWithLoadingState(block: suspend CoroutineScope.() -> Unit) {
        launch {
            sessionComponentCallback.onLoading(true)
            block()
            sessionComponentCallback.onLoading(false)
        }
    }

    private fun onState(state: T) {
        // TODO sessions: provide merchants with state
        sessionComponentCallback.onStateChanged(state)
    }

    private fun onComponentError(error: ComponentError) {
        sessionComponentCallback.onError(error)
    }

    private fun onSessionError(throwable: Throwable) {
        sessionComponentCallback.onError(
            ComponentError(
                CheckoutException(throwable.message.orEmpty(), throwable)
            )
        )
    }

    private fun onFinished(resultCode: String) {
        Log.d(TAG, "Finished: $resultCode")
        sessionComponentCallback.onFinished(resultCode)
    }

    private fun setFlowTakenOver() {
        if (sessionSavedStateHandleContainer.isFlowTakenOver == true) return
        sessionSavedStateHandleContainer.isFlowTakenOver = true
        Logger.i(TAG, "Flow was taken over.")
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
