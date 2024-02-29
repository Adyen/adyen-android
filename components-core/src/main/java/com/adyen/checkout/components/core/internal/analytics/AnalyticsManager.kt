/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.internal.analytics.data.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsManager internal constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val analyticsSetupProvider: AnalyticsSetupProvider,
    private val analyticsParams: AnalyticsParams,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private var checkoutAttemptId: String? = null

    private var isInitialized: Boolean = false

    // TODO: Check if we need to retry in case the request failed
    fun initialize(coroutineScope: CoroutineScope) {
        if (isInitialized) return

        isInitialized = true

        if (cannotSendEvent()) {
            checkoutAttemptId = CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS
            return
        }

        coroutineScope.launch(coroutineDispatcher) {
            runSuspendCatching {
                analyticsRepository.fetchCheckoutAttemptId(analyticsSetupProvider)
            }.fold(
                onSuccess = { checkoutAttemptId = it },
                onFailure = { adyenLog(AdyenLogLevel.WARN, it) { "Failed to fetch checkoutAttemptId." } },
            )
        }
    }

    private fun cannotSendEvent(): Boolean {
        return analyticsParams.level.priority <= AnalyticsParamsLevel.NONE.priority
    }

    // TODO: Work on coroutineScope. Perhaps implement a new one in this class.
    // TODO: This function should not be suspend, when coroutineScope is implemented.
    suspend fun trackEvent(event: AnalyticsEvent) {
        analyticsRepository.storeEvent(event)
    }

    fun getCheckoutAttemptId(): String? = checkoutAttemptId

    fun clear() {
        checkoutAttemptId = null
    }

    companion object {
        private const val CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS = "do-not-track"
    }
}
