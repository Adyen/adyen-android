/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AnalyticsManager {

    fun initialize(owner: Any, coroutineScope: CoroutineScope)

    fun trackEvent(event: AnalyticsEvent)

    fun getCheckoutAttemptId(): String

    fun clear(owner: Any)
}
