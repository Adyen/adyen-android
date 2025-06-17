/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.sessions.core.internal.CheckoutSessionInitializer

object CheckoutSessionProvider {

    /**
     * Allows creating a [CheckoutSession] from the response of the /sessions endpoint.
     * This is a suspend function that executes a network call on the IO thread.
     *
     * @param sessionModel The deserialized JSON response of the /sessions API call. You can use
     * [SessionModel.SERIALIZER] to deserialize this JSON.
     * @param configuration A [Configuration] to initialize the session. You can use the same configuration required to
     * initialize Drop-in or a component.
     * @param order An [Order] in case of an ongoing partial payment flow.
     *
     * @return The result of the API call.
     */
    suspend fun createSession(
        sessionModel: SessionModel,
        configuration: Configuration,
        order: Order? = null,
    ): CheckoutSessionResult {
        return createSession(
            sessionModel = sessionModel,
            environment = configuration.environment,
            clientKey = configuration.clientKey,
            order = order,
        )
    }

    /**
     * Allows creating a [CheckoutSession] from the response of the /sessions endpoint.
     * This is a suspend function that executes a network call on the IO thread.
     *
     * @param sessionModel The deserialized JSON response of the /sessions API call. You can use
     * [SessionModel.SERIALIZER] to deserialize this JSON.
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     * @param order An [Order] in case of an ongoing partial payment flow.
     *
     * @return The result of the API call.
     */
    suspend fun createSession(
        sessionModel: SessionModel,
        environment: Environment,
        clientKey: String,
        order: Order? = null,
    ): CheckoutSessionResult {
        return CheckoutSessionInitializer(sessionModel, environment, clientKey, order).setupSession(null)
    }

    /**
     * Only to be used for initializing a component for partial payment flow.
     *
     * Allows creating a [CheckoutSession] from the response of the /sessions endpoint.
     * This is a suspend function that executes a network call on the IO thread.
     *
     * @param sessionPaymentResult The [SessionPaymentResult] object to initialize the session. You will get this
     * object via [com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback.onPartialPayment] callback after
     * a partial payment has been done.
     * @param configuration A [Configuration] to initialize the session. You can use the same configuration required to
     * initialize Drop-in or a component.
     *
     * @return The result of the API call.
     */
    suspend fun createSession(
        sessionPaymentResult: SessionPaymentResult,
        configuration: Configuration,
    ): CheckoutSessionResult {
        return createSession(
            sessionPaymentResult = sessionPaymentResult,
            environment = configuration.environment,
            clientKey = configuration.clientKey,
        )
    }

    /**
     * Only to be used for initializing a component for partial payment flow.
     *
     * Allows creating a [CheckoutSession] from the response of the /sessions endpoint.
     * This is a suspend function that executes a network call on the IO thread.
     *
     * @param sessionPaymentResult The [SessionPaymentResult] object to initialize the session. You will get this
     * object via [com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback.onPartialPayment] callback after
     * a partial payment has been done.
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     *
     * @return The result of the API call.
     */
    suspend fun createSession(
        sessionPaymentResult: SessionPaymentResult,
        environment: Environment,
        clientKey: String,
    ): CheckoutSessionResult {
        if (sessionPaymentResult.sessionId == null) {
            throw CheckoutException("sessionId must not be null to create a session.")
        }
        val sessionModel = SessionModel(sessionPaymentResult.sessionId, sessionPaymentResult.sessionData)
        val order = sessionPaymentResult.order?.let { orderResponse ->
            Order(orderResponse.pspReference, orderResponse.orderData)
        }
        return CheckoutSessionInitializer(sessionModel, environment, clientKey, order)
            .setupSession(sessionPaymentResult.order?.remainingAmount)
    }
}
