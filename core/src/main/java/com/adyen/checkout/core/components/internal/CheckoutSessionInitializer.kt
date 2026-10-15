/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionResponse
import com.adyen.checkout.core.sessions.internal.SessionStateStore
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository

internal class CheckoutSessionInitializer(
    private val sessionRepository: SessionRepository,
    private val sessionStateStore: SessionStateStore,
) {

    suspend fun initialize(
        sessionResponse: SessionResponse,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        // TODO - Do we need to pass order here?
        sessionRepository.setupSession(sessionResponse.id, sessionResponse.sessionData, null)
            .fold(
                onSuccess = { response ->
                    // Setup is a fresh response from the server, so it replaces any stored value, also a restored one.
                    sessionStateStore.setSessionData(response.id, response.sessionData)
                    return CheckoutSession(
                        // TODO - Check if this still needs to be overridden with local amount
                        sessionSetupResponse = response.copy(amount = checkoutConfiguration.amount),
                        // TODO - Check if orders need to be supported
                        order = null,
                        environment = checkoutConfiguration.environment,
                        clientKey = checkoutConfiguration.clientKey,
                    )
                },
                onFailure = {
                    return null
                },
            )
    }
}
