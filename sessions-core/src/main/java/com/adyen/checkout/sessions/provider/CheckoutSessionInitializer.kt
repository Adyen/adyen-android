/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.provider

import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.model.SessionModel
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class CheckoutSessionInitializer(
    private val sessionModel: SessionModel,
    configuration: Configuration,
    private val order: OrderRequest?,
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
