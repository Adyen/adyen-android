/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.repository

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.api.SessionBalanceService
import com.adyen.checkout.sessions.api.SessionCancelOrderService
import com.adyen.checkout.sessions.api.SessionCreateOrderService
import com.adyen.checkout.sessions.api.SessionDetailsService
import com.adyen.checkout.sessions.api.SessionPaymentsService
import com.adyen.checkout.sessions.api.SessionSetupService
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.orders.SessionBalanceRequest
import com.adyen.checkout.sessions.model.orders.SessionBalanceResponse
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import com.adyen.checkout.sessions.model.orders.SessionOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionDetailsRequest
import com.adyen.checkout.sessions.model.payments.SessionDetailsResponse
import com.adyen.checkout.sessions.model.payments.SessionPaymentsRequest
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.model.setup.SessionSetupRequest
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse
import org.json.JSONException
import java.io.IOException

class SessionRepository {

    suspend fun setupSession(
        configuration: Configuration,
        session: Session,
        order: OrderRequest?
    ): SessionSetupResponse {
        Logger.d(TAG, "Setting up session")
        try {
            val request = SessionSetupRequest(session.sessionData.orEmpty(), order)
            return SessionSetupService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).setupSession()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionSetupConnection Failed", e)
            throw CheckoutException("Unable to setup session")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionSetupConnection unexpected result", e)
            throw CheckoutException("Unable to setup session")
        }
    }

    suspend fun submitPayment(
        configuration: Configuration,
        session: Session,
        paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
    ): SessionPaymentsResponse {
        Logger.d(TAG, "Submitting payment")
        try {
            val request = SessionPaymentsRequest(session.sessionData.orEmpty(), paymentComponentData)
            return SessionPaymentsService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).submitPayment()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionPaymentsConnection Failed", e)
            throw CheckoutException("Unable to submit payment")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionPaymentsConnection unexpected result", e)
            throw CheckoutException("Unable to submit payment")
        }
    }

    suspend fun submitDetails(
        configuration: Configuration,
        session: Session,
        actionComponentData: ActionComponentData
    ): SessionDetailsResponse {
        Logger.d(TAG, "Submitting details")
        try {
            val request = SessionDetailsRequest(
                sessionData = session.sessionData.orEmpty(),
                paymentData = actionComponentData.paymentData,
                details = actionComponentData.details
            )
            return SessionDetailsService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).submitDetails()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionDetailsConnection Failed", e)
            throw CheckoutException("Unable to submit details")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionDetailsConnection unexpected result", e)
            throw CheckoutException("Unable to submit details")
        }
    }

    suspend fun checkBalance(
        configuration: Configuration,
        session: Session,
        paymentMethodDetails: PaymentMethodDetails
    ): SessionBalanceResponse {
        Logger.d(TAG, "Checking payment method balance")
        try {
            val request = SessionBalanceRequest(session.sessionData.orEmpty(), paymentMethodDetails)
            return SessionBalanceService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).checkBalance()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionBalanceConnection Failed", e)
            throw CheckoutException("Unable to fetch balance")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionBalanceConnection unexpected result", e)
            throw CheckoutException("Unable to fetch balance")
        }
    }

    suspend fun createOrder(
        configuration: Configuration,
        session: Session
    ): SessionOrderResponse {
        Logger.d(TAG, "Creating order")
        try {
            val request = SessionOrderRequest(session.sessionData.orEmpty())
            return SessionCreateOrderService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).createOrder()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionCreateOrderConnection Failed", e)
            throw CheckoutException("Unable to create order")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionCreateOrderConnection unexpected result", e)
            throw CheckoutException("Unable to create order")
        }
    }

    suspend fun cancelOrder(
        configuration: Configuration,
        session: Session,
        order: OrderRequest
    ): SessionCancelOrderResponse {
        Logger.d(TAG, "Cancelling order")
        try {
            val request = SessionCancelOrderRequest(session.sessionData.orEmpty(), order)
            return SessionCancelOrderService(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).cancelOrder()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionCancelOrderConnection Failed", e)
            throw CheckoutException("Unable to cancel order")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionCancelOrderConnection unexpected result", e)
            throw CheckoutException("Unable to cancel order")
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
