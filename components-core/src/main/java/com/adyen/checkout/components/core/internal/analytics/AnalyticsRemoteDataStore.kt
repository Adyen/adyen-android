/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

internal interface AnalyticsRemoteDataStore {

    suspend fun fetchCheckoutAttemptId(): String

    suspend fun sendEvents()
}
