/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/1/2023.
 */

package com.adyen.checkout.sessions.core.internal

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
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("TooManyFunctions")
class SessionComponentEventHandler<T : PaymentComponentState<*>>(
    private val sessionInteractor: SessionInteractor,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
) : ComponentEventHandler<T> {

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

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, componentCallback: BaseComponentCallback) {
        @Suppress("UNCHECKED_CAST")
        val sessionComponentCallback = componentCallback as? SessionComponentCallback<T>
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

            is PaymentComponentEvent.Submit -> onPaymentsCallRequested(event.state, sessionComponentCallback)
        }
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
        sessionComponentCallback: SessionComponentCallback<T>
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

    private fun onPermissionRequest(
        requiredPermission: String,
        permissionCallback: PermissionHandlerCallback,
        sessionComponentCallback: SessionComponentCallback<T>
    ) {
        sessionComponentCallback.onPermissionRequest(requiredPermission, permissionCallback)
    }

    private fun onComponentError(error: ComponentError, sessionComponentCallback: SessionComponentCallback<T>) {
        sessionComponentCallback.onError(error)
    }

    private fun onSessionError(throwable: Throwable, sessionComponentCallback: SessionComponentCallback<T>) {
        sessionComponentCallback.onError(
            ComponentError(
                CheckoutException(throwable.message.orEmpty(), throwable),
            ),
        )
    }

    private fun onFinished(result: SessionPaymentResult, sessionComponentCallback: SessionComponentCallback<T>) {
        adyenLog(AdyenLogLevel.DEBUG) { "Finished: ${result.resultCode}" }
        sessionComponentCallback.onFinished(result)
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
