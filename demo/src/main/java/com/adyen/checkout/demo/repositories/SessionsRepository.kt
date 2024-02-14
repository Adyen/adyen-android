/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.repositories

import com.adyen.checkout.demo.data.api.CheckoutApiService
import com.adyen.checkout.demo.data.api.model.SessionRequest
import com.adyen.checkout.sessions.core.SessionModel

interface SessionsRepository {
    suspend fun createSession(sessionRequest: SessionRequest): SessionModel?
}

class SessionsRepositoryImpl(private val checkoutApiService: CheckoutApiService) : SessionsRepository {
    override suspend fun createSession(sessionRequest: SessionRequest): SessionModel? = safeApiCall {
        checkoutApiService.sessionsAsync(sessionRequest)
    }
}
