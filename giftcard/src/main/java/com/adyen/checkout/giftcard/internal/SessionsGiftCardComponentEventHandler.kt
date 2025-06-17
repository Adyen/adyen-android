/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/3/2023.
 */

package com.adyen.checkout.giftcard.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.PermissionHandlerCallback
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.giftcard.GiftCardAction
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardException
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.sessions.core.internal.SessionCallResult
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionsGiftCardComponentEventHandler(
    private val sessionInteractor: SessionInteractor,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
) : ComponentEventHandler<GiftCardComponentState> {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        sessionInteractor.sessionFlow
            .mapNotNull { it.sessionData }
            .onEach { updateSessionData(it) }
            .launchIn(coroutineScope)
    }

    private fun updateSessionData(sessionData: String) {
        adyenLog(AdyenLogLevel.VERBOSE) { "Updating session data - $sessionData" }
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }

    override fun onPaymentComponentEvent(
        event: PaymentComponentEvent<GiftCardComponentState>,
        componentCallback: BaseComponentCallback
    ) {
        val sessionComponentCallback = componentCallback as? SessionsGiftCardComponentCallback
            ?: throw CheckoutException("Callback must be type of ${SessionComponentCallback::class.java.canonicalName}")
        adyenLog(AdyenLogLevel.VERBOSE) { "Event received $event" }
        when (event) {
            is PaymentComponentEvent.ActionDetails -> onDetailsCallRequested(event.data, sessionComponentCallback)
            is PaymentComponentEvent.Error -> onComponentError(event.error, sessionComponentCallback)
            is PaymentComponentEvent.StateChanged -> onState(event.state, sessionComponentCallback)
            is PaymentComponentEvent.PermissionRequest -> onPermissionRequest(
                event.requiredPermission,
                event.permissionCallback,
                sessionComponentCallback,
            )

            is PaymentComponentEvent.Submit -> {
                when (event.state.giftCardAction) {
                    GiftCardAction.CheckBalance -> {
                        event.state.data.paymentMethod?.let {
                            checkBalance(event.state, sessionComponentCallback)
                        } ?: throw GiftCardException("Payment method is null.")
                    }

                    GiftCardAction.CreateOrder -> createOrder(sessionComponentCallback)
                    GiftCardAction.SendPayment -> onPaymentsCallRequested(event.state, sessionComponentCallback)
                    GiftCardAction.Idle -> {} // no ops
                }
            }
        }
    }

    private fun onPaymentsCallRequested(
        paymentComponentState: GiftCardComponentState,
        sessionComponentCallback: SessionsGiftCardComponentCallback
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
                is SessionCallResult.Payments.NotFullyPaidOrder -> {
                    onPartialPayment(result, sessionComponentCallback)
                }

                is SessionCallResult.Payments.RefusedPartialPayment -> onFinished(
                    result.result,
                    sessionComponentCallback,
                )

                is SessionCallResult.Payments.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                sessionComponentCallback::onAdditionalDetails,
                sessionComponentCallback::onAdditionalDetails.name,
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
        paymentComponentState: PaymentComponentState<*>,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.checkBalance(
                paymentComponentState,
                sessionComponentCallback::onBalanceCheck,
                sessionComponentCallback::onBalanceCheck.name,
            )
            when (result) {
                is SessionCallResult.Balance.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.Balance.Successful -> {
                    sessionComponentCallback.onBalance(result.balanceResult)
                }

                SessionCallResult.Balance.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun createOrder(sessionComponentCallback: SessionsGiftCardComponentCallback) {
        coroutineScope.launchWithLoadingState(sessionComponentCallback) {
            val result = sessionInteractor.createOrder(
                sessionComponentCallback::onOrderRequest,
                sessionComponentCallback::onOrderRequest.name,
            )

            when (result) {
                is SessionCallResult.CreateOrder.Error -> onSessionError(result.throwable, sessionComponentCallback)
                is SessionCallResult.CreateOrder.Successful -> {
                    sessionComponentCallback.onOrder(result.order)
                }

                SessionCallResult.CreateOrder.TakenOver -> {
                    setFlowTakenOver()
                }
            }
        }
    }

    private fun CoroutineScope.launchWithLoadingState(
        sessionComponentCallback: SessionsGiftCardComponentCallback,
        block: suspend CoroutineScope.() -> Unit
    ) {
        launch {
            sessionComponentCallback.onLoading(true)
            block()
            sessionComponentCallback.onLoading(false)
        }
    }

    private fun onState(
        state: GiftCardComponentState,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        sessionComponentCallback.onStateChanged(state)
    }

    private fun onPermissionRequest(
        requiredPermission: String,
        permissionCallback: PermissionHandlerCallback,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        sessionComponentCallback.onPermissionRequest(requiredPermission, permissionCallback)
    }

    private fun onComponentError(
        error: ComponentError,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        sessionComponentCallback.onError(error)
    }

    private fun onSessionError(
        throwable: Throwable,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        sessionComponentCallback.onError(
            ComponentError(
                CheckoutException(throwable.message.orEmpty(), throwable),
            ),
        )
    }

    private fun onFinished(
        result: SessionPaymentResult,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        adyenLog(AdyenLogLevel.DEBUG) { "Finished: ${result.resultCode}" }
        sessionComponentCallback.onFinished(result)
    }

    private fun onPartialPayment(
        sessionCallResult: SessionCallResult.Payments.NotFullyPaidOrder,
        sessionComponentCallback: SessionsGiftCardComponentCallback
    ) {
        adyenLog(AdyenLogLevel.DEBUG) { "Partial payment: ${sessionCallResult.result.order}" }
        sessionComponentCallback.onPartialPayment(sessionCallResult.result)
    }

    private fun setFlowTakenOver() {
        if (sessionSavedStateHandleContainer.isFlowTakenOver == true) return
        sessionSavedStateHandleContainer.isFlowTakenOver = true
        adyenLog(AdyenLogLevel.INFO) { "Flow was taken over." }
    }

    override fun onCleared() {
        _coroutineScope = null
    }
}
