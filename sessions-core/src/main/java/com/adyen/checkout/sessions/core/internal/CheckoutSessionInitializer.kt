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
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CheckoutSessionInitializer(
    private val sessionModel: SessionModel,
    configuration: Configuration,
    private val order: Order?,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val httpClient = HttpClientFactory.getHttpClient(configuration.environment)
    private val sessionService = SessionService(httpClient)
    private val sessionRepository = SessionRepository(sessionService, configuration.clientKey)

    // TODO: Once Backend provides the correct amount in the SessionSetupResponse use that in SessionDetails instead of
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
                    ),
                )
            },
            onFailure = {
                return@withContext CheckoutSessionResult.Error(CheckoutException("Failed to fetch session", it))
            },
        )
    }
}
