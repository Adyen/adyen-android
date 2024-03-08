/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AnalyticsManager {

    fun initialize(owner: Any, coroutineScope: CoroutineScope)

    fun trackEvent(event: AnalyticsEvent)

    fun getCheckoutAttemptId(): String?

    fun clear(owner: Any)
}
