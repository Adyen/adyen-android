/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2022.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.example.data.api.RecurringApiService
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface RecurringRepository {
    suspend fun removeStoredPaymentMethod(requestBody: RequestBody): ResponseBody?
}

class RecurringRepositoryImpl(private val recurringApiService: RecurringApiService) : RecurringRepository, BaseRepository() {
    override suspend fun removeStoredPaymentMethod(requestBody: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { recurringApiService.removeStoredPaymentMethodAsync(requestBody).await() }
        )
    }
}
