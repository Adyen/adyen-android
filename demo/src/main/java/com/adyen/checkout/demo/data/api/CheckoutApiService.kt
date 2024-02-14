/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.data.api

import com.adyen.checkout.demo.data.api.model.SessionRequest
import com.adyen.checkout.sessions.core.SessionModel
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckoutApiService {
    @POST("sessions")
    suspend fun sessionsAsync(@Body sessionRequest: SessionRequest): SessionModel
}
