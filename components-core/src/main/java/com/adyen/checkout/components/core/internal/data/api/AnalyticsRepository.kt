/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo

// TODO: Remove this file
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AnalyticsRepository {
    suspend fun setupAnalytics()

    fun getCheckoutAttemptId(): String?
}
