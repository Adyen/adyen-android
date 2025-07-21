/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.Order
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionModel

internal object CheckoutSessionProvider {

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
        configuration: CheckoutConfiguration,
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
    @Suppress("CommentWrapping")
    suspend fun createSession(
        sessionModel: SessionModel,
        environment: Environment,
        clientKey: String,
        order: Order? = null,
    ): CheckoutSessionResult {
        return CheckoutSessionInitializer(
            sessionModel = sessionModel,
            environment = environment,
            clientKey = clientKey,
            order = order,
        ).setupSession(null)
    }
}
