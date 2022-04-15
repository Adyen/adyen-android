/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2022.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.example.data.api.RecurringApiService
import com.adyen.checkout.example.data.api.model.RemoveStoredPaymentMethodRequest
import org.json.JSONObject

interface RecurringRepository {
    suspend fun removeStoredPaymentMethod(request: RemoveStoredPaymentMethodRequest): JSONObject?
}

internal class RecurringRepositoryImpl(
    private val recurringApiService: RecurringApiService
) : RecurringRepository {

    override suspend fun removeStoredPaymentMethod(request: RemoveStoredPaymentMethodRequest): JSONObject? =
        safeApiCall { recurringApiService.removeStoredPaymentMethodAsync(request) }
}
