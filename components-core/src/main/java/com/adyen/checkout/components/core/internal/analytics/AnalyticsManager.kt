/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import kotlinx.coroutines.CoroutineScope

class AnalyticsManager internal constructor() {

    private var checkoutAttemptId: String? = null

    private var isInitialized: Boolean = false

    fun initialize(coroutineScope: CoroutineScope) {

    }

    fun trackEvent(event: AnalyticsEvent) {

    }

    fun getCheckoutAttemptId(): String? = checkoutAttemptId

    fun clear() {}
}
