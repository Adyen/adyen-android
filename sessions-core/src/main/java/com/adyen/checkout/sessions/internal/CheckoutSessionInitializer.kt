/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.internal

import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.CheckoutSessionResult
import com.adyen.checkout.sessions.SessionModel
import com.adyen.checkout.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class CheckoutSessionInitializer(
    private val sessionModel: SessionModel,
    configuration: Configuration,
    private val order: Order?,
) {
    private val httpClient = HttpClientFactory.getHttpClient(configuration.environment)
    private val sessionService = SessionService(httpClient)
    private val sessionRepository = SessionRepository(sessionService, configuration.clientKey)

    suspend fun setupSession(): CheckoutSessionResult {
        sessionRepository.setupSession(
            sessionModel = sessionModel,
            order = order,
        ).fold(
            onSuccess = {
                return CheckoutSessionResult.Success(CheckoutSession(it, order))
            },
            onFailure = {
                return CheckoutSessionResult.Error(CheckoutException("Failed to fetch session", it))
            }
        )
    }

    fun setupSession(
        coroutineScope: CoroutineScope,
        callback: (CheckoutSessionResult) -> Unit
    ) {
        coroutineScope.launch {
            callback(setupSession())
        }
    }
}
