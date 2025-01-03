/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/4/2022.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.internal.service.BaseDropInService
import com.adyen.checkout.dropin.internal.service.SessionDropInServiceInterface
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.internal.SessionCallResult
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * Extend this service if you want to take over the sessions flow in Drop-in and make the required network calls to the
 * Adyen Checkout APIs through your backend. Note that once you take over the sessions flow you have to handle the rest
 * of the network calls yourself.
 *
 * Make sure you define add your subclass of this [SessionDropInService] in your manifest file.
 */
open class SessionDropInService : BaseDropInService(), SessionDropInServiceInterface, SessionDropInServiceContract {

    private lateinit var sessionInteractor: SessionInteractor

    /**
     * Indicates whether you already took over the sessions flow in a previous action. This field could be useful if you
     * are only taking over the flow in certain conditions and you want to know in a subsequent call whether you did
     * take over the flow earlier or not.
     */
    var isFlowTakenOver: Boolean = false
        private set

    final override fun initialize(
        sessionModel: SessionModel,
        clientKey: String,
        environment: Environment,
        isFlowTakenOver: Boolean,
        analyticsManager: AnalyticsManager,
    ) {
        val httpClient = HttpClientFactory.getHttpClient(environment)
        val sessionService = SessionService(httpClient)
        sessionInteractor = SessionInteractor(
            sessionRepository = SessionRepository(
                sessionService = sessionService,
                clientKey = clientKey,
            ),
            sessionModel = sessionModel,
            isFlowTakenOver = isFlowTakenOver,
            analyticsManager = analyticsManager,
        )
        this.isFlowTakenOver = isFlowTakenOver

        launch {
            sessionInteractor.sessionFlow
                .mapNotNull { it.sessionData }
                .collect { sendSessionDataChangedResult(it) }
        }
    }

    private fun sendSessionDataChangedResult(sessionData: String) {
        adyenLog(AdyenLogLevel.DEBUG) { "Sending session data changed result - $sessionData" }
        val result = SessionDropInServiceResult.SessionDataChanged(sessionData)
        emitResult(result)
    }

    final override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        launch {
            val result = sessionInteractor.onPaymentsCallRequested(
                paymentComponentState,
                ::onSubmit,
                ::onSubmit.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Payments.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Payments.Error ->
                    DropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                        reason = result.throwable.message,
                        dismissDropIn = true,
                    )

                is SessionCallResult.Payments.Finished -> SessionDropInServiceResult.Finished(result.result)
                is SessionCallResult.Payments.NotFullyPaidOrder -> updatePaymentMethods(result.result.order)
                is SessionCallResult.Payments.RefusedPartialPayment ->
                    DropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                        reason = "Payment was refused while making a partial payment",
                    )

                is SessionCallResult.Payments.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            emitResult(dropInServiceResult)
        }
    }

    final override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        launch {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                ::onAdditionalDetails,
                ::onAdditionalDetails.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Details.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Details.Error ->
                    DropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                        reason = result.throwable.message,
                        dismissDropIn = true,
                    )

                is SessionCallResult.Details.Finished -> SessionDropInServiceResult.Finished(result.result)
                SessionCallResult.Details.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            emitResult(dropInServiceResult)
        }
    }

    final override fun requestBalanceCall(paymentComponentState: PaymentComponentState<*>) {
        launch {
            val result = sessionInteractor.checkBalance(
                paymentComponentState,
                ::onBalanceCheck,
                ::onBalanceCheck.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Balance.Error ->
                    BalanceDropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                        reason = result.throwable.message,
                    )

                is SessionCallResult.Balance.Successful -> BalanceDropInServiceResult.Balance(result.balanceResult)
                SessionCallResult.Balance.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendBalanceResult(dropInServiceResult)
        }
    }

    final override fun requestOrdersCall() {
        launch {
            val result = sessionInteractor.createOrder(
                ::onOrderRequest,
                ::onOrderRequest.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CreateOrder.Error ->
                    OrderDropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                        reason = result.throwable.message,
                        dismissDropIn = true,
                    )

                is SessionCallResult.CreateOrder.Successful -> OrderDropInServiceResult.OrderCreated(result.order)
                SessionCallResult.CreateOrder.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendOrderResult(dropInServiceResult)
        }
    }

    final override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        val shouldUpdatePaymentMethods = !isDropInCancelledByUser
        launch {
            val result = sessionInteractor.cancelOrder(
                order,
                { onOrderCancel(it, shouldUpdatePaymentMethods) },
                ::onOrderCancel.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CancelOrder.Error ->
                    DropInServiceResult.Error(
                        errorDialog = ErrorDialog(message = getString(R.string.unknown_error)),
                        reason = result.throwable.message,
                    )

                SessionCallResult.CancelOrder.Successful -> {
                    if (!shouldUpdatePaymentMethods) return@launch
                    updatePaymentMethods()
                }

                SessionCallResult.CancelOrder.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    override fun onRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        launch {
            val storedPaymentMethodId = storedPaymentMethod.id.orEmpty()
            val result = sessionInteractor.removeStoredPaymentMethod(storedPaymentMethodId)

            val serviceResult = when (result) {
                SessionCallResult.RemoveStoredPaymentMethod.Successful ->
                    RecurringDropInServiceResult.PaymentMethodRemoved(
                        storedPaymentMethodId,
                    )

                is SessionCallResult.RemoveStoredPaymentMethod.Error -> RecurringDropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = getString(R.string.unknown_error)),
                    reason = result.throwable.message,
                )
            }

            sendRecurringResult(serviceResult)
        }
    }

    private suspend fun updatePaymentMethods(order: OrderResponse? = null): DropInServiceResult {
        return when (val result = sessionInteractor.updatePaymentMethods(order)) {
            is SessionCallResult.UpdatePaymentMethods.Successful -> DropInServiceResult.Update(
                result.paymentMethods,
                result.order,
            )

            is SessionCallResult.UpdatePaymentMethods.Error ->
                DropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = getString(R.string.payment_failed)),
                    reason = result.throwable.message,
                    dismissDropIn = true,
                )
        }
    }

    private fun sendFlowTakenOverUpdatedResult() {
        if (isFlowTakenOver) return
        isFlowTakenOver = true
        adyenLog(AdyenLogLevel.INFO) { "Flow was taken over, sending update to drop-in" }
        val result = SessionDropInServiceResult.SessionTakenOverUpdated(true)
        emitResult(result)
    }
}
