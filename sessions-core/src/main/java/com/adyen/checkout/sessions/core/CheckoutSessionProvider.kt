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
        return CheckoutSessionInitializer(sessionModel, configuration, order).setupSession()
    }
}
