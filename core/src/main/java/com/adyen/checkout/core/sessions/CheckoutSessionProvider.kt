/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.Environment

internal object CheckoutSessionProvider {

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
        // TODO - Partial Payment Flow
//        order: Order? = null,
    ): CheckoutSessionResult {
        return CheckoutSessionInitializer(sessionModel, environment, clientKey).setupSession(null)
    }
}
