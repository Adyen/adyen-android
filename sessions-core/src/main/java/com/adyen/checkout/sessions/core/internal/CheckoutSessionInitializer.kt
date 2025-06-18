/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.core.internal

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.data.api.HttpClientFactory
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class CheckoutSessionInitializer(
    private val sessionModel: SessionModel,
    private val environment: Environment,
    private val clientKey: String,
    private val order: Order?,
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
            order = order,
        ).fold(
            onSuccess = { sessionSetupResponse ->
                return@withContext CheckoutSessionResult.Success(
                    CheckoutSession(
                        sessionSetupResponse.copy(amount = overrideAmount ?: sessionSetupResponse.amount),
                        order,
                        environment,
                        clientKey,
                    ),
                )
            },
            onFailure = {
                return@withContext CheckoutSessionResult.Error(CheckoutException("Failed to fetch session", it))
            },
        )
    }
}
