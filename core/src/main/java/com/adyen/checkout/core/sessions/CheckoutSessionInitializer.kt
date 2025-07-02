/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class CheckoutSessionInitializer(
    private val sessionModel: SessionModel,
    private val environment: Environment,
    private val clientKey: String,
    // TODO - Partial Payment Flow
//    private val order: Order?,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    private val httpClient = HttpClientFactory.getHttpClient(environment)
    private val sessionService = SessionService(httpClient)
    private val sessionRepository = SessionRepository(sessionService, clientKey)

    // TODO Once Backend provides the correct amount in the SessionSetupResponse use that in SessionDetails instead of
    //  override Amount
    suspend fun setupSession(overrideAmount: Amount?): CheckoutSessionResult = withContext(coroutineDispatcher) {
        sessionRepository.setupSession(
            sessionModel = sessionModel,
//            order = order,
        ).fold(
            onSuccess = { sessionSetupResponse ->
                return@withContext CheckoutSessionResult.Success(
                    CheckoutSession(
                        sessionSetupResponse.copy(amount = overrideAmount ?: sessionSetupResponse.amount),
//                        order,
                        environment,
                        clientKey,
                    ),
                )
            },
            onFailure = {
                // TODO - Error propagation
//                return@withContext CheckoutSessionResult.Error(CheckoutException("Failed to fetch session", it))
                return@withContext CheckoutSessionResult.Error(Exception("Failed to fetch session", it))
            },
        )
    }
}
