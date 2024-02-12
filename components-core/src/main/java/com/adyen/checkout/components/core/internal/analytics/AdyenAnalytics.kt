/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AdyenAnalytics(
    private val analyticsProvider: AnalyticsProvider,
    private val analyticsParams: AnalyticsParams,
    private val analyticsService: AnalyticsService,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    // TODO: Check if Job or SupervisorJob is better for us
    private val coroutineScope = CoroutineScope(coroutineDispatcher + SupervisorJob())

    @Volatile
    var checkoutAttemptId: String? = null
        private set

    @Volatile
    private var state: State = State.Uninitialized

    fun setup() {
        if (cannotSendEvent()) {
            checkoutAttemptId = CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS
            return
        }

        if (state != State.Uninitialized) return
        state = State.InProgress
        adyenLog(AdyenLogLevel.VERBOSE) { "Setting up analytics" }

        coroutineScope.launch {
            runSuspendCatching {
                val analyticsSetupRequest = analyticsProvider.provide()
                val response = analyticsService.setupAnalytics(analyticsSetupRequest, analyticsParams.clientKey)
                checkoutAttemptId = response.checkoutAttemptId
                state = State.Ready
                adyenLog(AdyenLogLevel.VERBOSE) { "Analytics setup call successful" }
            }.onFailure { e ->
                state = State.Failed
                adyenLog(AdyenLogLevel.ERROR) {
                    "Failed to send analytics setup call - ${e::class.simpleName}: ${e.message}"
                }
            }
        }
    }

    fun track(event: AnalyticsEvent) {
        // TODO: Check if we can send events anyway, because attempt id is anonymous already
        if (cannotSendEvent()) return

        // Queue the event
        // Send it
    }

    private fun cannotSendEvent(): Boolean {
        return analyticsParams.level.priority <= AnalyticsParamsLevel.NONE.priority
    }

    companion object {
        private const val CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS = "do-not-track"
    }

    @VisibleForTesting
    internal sealed class State {
        data object Uninitialized : State()
        data object InProgress : State()
        data object Ready : State()
        data object Failed : State()
    }
}
