/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.repository

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.api.SessionBalanceConnection
import com.adyen.checkout.sessions.api.SessionCancelOrderConnection
import com.adyen.checkout.sessions.api.SessionCreateOrderConnection
import com.adyen.checkout.sessions.api.SessionDetailsConnection
import com.adyen.checkout.sessions.api.SessionPaymentsConnection
import com.adyen.checkout.sessions.api.SessionSetupConnection
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.orders.SessionBalanceRequest
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.payments.SessionDetailsRequest
import com.adyen.checkout.sessions.model.orders.SessionOrderRequest
import com.adyen.checkout.sessions.model.payments.SessionPaymentsRequest
import com.adyen.checkout.sessions.model.setup.SessionSetupRequest
import com.adyen.checkout.sessions.model.orders.SessionBalanceResponse
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionDetailsResponse
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse
import java.io.IOException
import org.json.JSONException

class SessionRepository {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    suspend fun setupSession(
        configuration: Configuration,
        session: Session,
        order: OrderRequest?
    ): SessionSetupResponse {
        Logger.d(TAG, "Setting up session")
        try {
            val request = SessionSetupRequest(session.sessionData.orEmpty(), order)
            return SessionSetupConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
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
            return SessionPaymentsConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
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
            val request = SessionDetailsRequest(session.sessionData.orEmpty(), actionComponentData.paymentData, actionComponentData.details)
            return SessionDetailsConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
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
            return SessionBalanceConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
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
            return SessionCreateOrderConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
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
            return SessionCancelOrderConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionCancelOrderConnection Failed", e)
            throw CheckoutException("Unable to cancel order")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionCancelOrderConnection unexpected result", e)
            throw CheckoutException("Unable to cancel order")
        }
    }
}
