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
import com.adyen.checkout.components.base.BaseComponentCallback
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.interactor.SessionCallResult
import com.adyen.checkout.sessions.interactor.SessionInteractor
import com.adyen.checkout.sessions.model.SessionPaymentResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("TooManyFunctions")
class SessionHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
) : ComponentEventHandler<T> {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        coroutineScope.launch {
            sessionInteractor.sessionFlow
                .mapNotNull { it.sessionData }
                .collect { updateSessionData(it) }
        }
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, componentCallback: BaseComponentCallback) {
        @Suppress("UNCHECKED_CAST")
        val sessionComponentCallback = componentCallback as? SessionComponentCallback<T>
            ?: throw CheckoutException("Callback must be type of ${SessionComponentCallback::class.java.canonicalName}")
        Logger.v(TAG, "Event received $event")
        when (event) {
            is PaymentComponentEvent.ActionDetails -> onDetailsCallRequested(event.data, sessionComponentCallback)
            is PaymentComponentEvent.Error -> onComponentError(event.error, sessionComponentCallback)
            is PaymentComponentEvent.StateChanged -> onState(event.state, sessionComponentCallback)
            is PaymentComponentEvent.Submit -> onPaymentsCallRequested(event.state, sessionComponentCallback)
        }
    }

    private fun updateSessionData(sessionData: String) {
        Logger.v(TAG, "Updating session data - $sessionData")
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }

    private fun onPaymentsCallRequested(
        paymentComponentState: T,
        sessionComponentCallback: SessionComponentCallback<T>
    ) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.onPaymentsCallRequested(
                paymentComponentState,
                sessionComponentCallback::onSubmit,
                sessionComponentCallback::onSubmit.name,
            )

            when (result) {
                is SessionCallResult.Payments.Action -> {
                    sessionComponentCallback.onAction(result.action)
                }
                is SessionCallResult.Payments.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.Payments.Finished -> onFinished(result.result, sessionComponentCallback)
                is SessionCallResult.Payments.NotFullyPaidOrder -> onFinished(result.result, sessionComponentCallback)
                is SessionCallResult.Payments.RefusedPartialPayment -> onFinished(
                    result.result,
                    sessionComponentCallback
                )
                is SessionCallResult.Payments.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        sessionComponentCallback: SessionComponentCallback<T>
    ) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                sessionComponentCallback::onAdditionalDetails,
                sessionComponentCallback::onAdditionalDetails.name
            )

            when (result) {
                is SessionCallResult.Details.Action -> {
                    sessionComponentCallback.onAction(result.action)
                }
                is SessionCallResult.Details.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.Details.Finished -> onFinished(result.result, sessionComponentCallback)
                SessionCallResult.Details.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun checkBalance(
        paymentMethodData: PaymentMethodDetails,
        sessionComponentCallback: SessionComponentCallback<T>
    ) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.checkBalance(
                paymentMethodData,
                sessionComponentCallback::onBalanceCheck,
                sessionComponentCallback::onBalanceCheck.name,
            )
            when (result) {
                is SessionCallResult.Balance.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.Balance.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.Balance.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun createOrder(sessionComponentCallback: SessionComponentCallback<T>) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.createOrder(
                sessionComponentCallback::onOrderRequest,
                sessionComponentCallback::onOrderRequest.name
            )

            when (result) {
                is SessionCallResult.CreateOrder.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.CreateOrder.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.CreateOrder.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun cancelOrder(order: OrderRequest, sessionComponentCallback: SessionComponentCallback<T>) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.cancelOrder(
                order,
                sessionComponentCallback::onOrderCancel,
                sessionComponentCallback::onOrderCancel.name
            )

            when (result) {
                is SessionCallResult.CancelOrder.Error -> onSessionError(result.throwable, sessionComponentCallback)
                SessionCallResult.CancelOrder.Successful -> {
                    // TODO sessions: handle with gift card
                }
                SessionCallResult.CancelOrder.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun CoroutineScope.launchWithLoadingState(
        sessionComponentCallback: SessionComponentCallback<T>,
        block: suspend CoroutineScope.() -> Unit
    ) {
        launch {
            sessionComponentCallback.onLoading(true)
            block()
            sessionComponentCallback.onLoading(false)
        }
    }

    private fun onState(state: T, sessionComponentCallback: SessionComponentCallback<T>) {
        sessionComponentCallback.onStateChanged(state)
    }

    private fun onComponentError(error: ComponentError, sessionComponentCallback: SessionComponentCallback<T>) {
        sessionComponentCallback.onError(error)
    }

    private fun onSessionError(throwable: Throwable, sessionComponentCallback: SessionComponentCallback<T>) {
        sessionComponentCallback.onError(
            ComponentError(
                CheckoutException(throwable.message.orEmpty(), throwable)
            )
        )
    }

    private fun onFinished(result: SessionPaymentResult, sessionComponentCallback: SessionComponentCallback<T>) {
        Log.d(TAG, "Finished: ${result.resultCode}")
        sessionComponentCallback.onFinished(result)
    }

    private fun setFlowTakenOver() {
        if (sessionSavedStateHandleContainer.isFlowTakenOver == true) return
        sessionSavedStateHandleContainer.isFlowTakenOver = true
        Logger.i(TAG, "Flow was taken over.")
    }

    override fun onCleared() {
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
