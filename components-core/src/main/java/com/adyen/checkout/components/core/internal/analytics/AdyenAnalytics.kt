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
import com.adyen.checkout.components.core.internal.data.api.AnalyticsTrackRequestMapper
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
// TODO: Create factory
class AdyenAnalytics internal constructor(
    private val analyticsProvider: AnalyticsProvider,
    private val analyticsParams: AnalyticsParams,
    private val analyticsService: AnalyticsService,
    private val analyticsTrackRequestMapper: AnalyticsTrackRequestMapper,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    // TODO: Check if Job or SupervisorJob is better for us
    private val coroutineScope = CoroutineScope(coroutineDispatcher + SupervisorJob())

    private val eventQueue: LinkedList<AnalyticsEvent> = LinkedList<AnalyticsEvent>()

    val checkoutAttemptId: String? get() = (state as? State.Ready)?.checkoutAttemptId

    @Volatile
    private var state: State = State.Uninitialized

    private val mutex = Mutex()

    fun setup() {
        coroutineScope.launch {
            setupInternal()
        }
    }

    private suspend fun setupInternal() {
        if (cannotSendEvent()) {
            state = State.Ready(CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS)
            return
        }

        if (!state.canInitialize()) return

        state = State.InProgress

        adyenLog(AdyenLogLevel.VERBOSE) { "Setting up analytics" }

        runSuspendCatching {
            adyenLog(AdyenLogLevel.VERBOSE) { "Analytics setup call successful" }
            state = fetchCheckoutAttemptId()?.let {
                adyenLog(AdyenLogLevel.VERBOSE) { "Analytics setup call successful" }
                State.Ready(it)
            } ?: run {
                adyenLog(AdyenLogLevel.WARN) { "checkoutAttemptId from response is null" }
                State.Failed
            }
        }.onFailure { e ->
            adyenLog(AdyenLogLevel.ERROR) {
                "Failed to send analytics setup call - ${e::class.simpleName}: ${e.message}"
            }
            state = State.Failed
        }
    }

    private suspend fun fetchCheckoutAttemptId(): String? {
        val analyticsSetupRequest = analyticsProvider.provide()
        val response = analyticsService.setupAnalytics(analyticsSetupRequest, analyticsParams.clientKey)
        return response.checkoutAttemptId
    }


    fun track(event: AnalyticsEvent) {
        if (cannotSendEvent()) return

        coroutineScope.launch {
            mutex.withLock {
                eventQueue.add(event)
                track(event)
            }
        }
    }

    // TODO: Discuss if we need to use mappers before we send events to backend
    private suspend fun sendEvents() {
        if (state.canInitialize()) {
            setupInternal()
        }

        val checkoutAttemptId = checkoutAttemptId ?: run {
            adyenLog(AdyenLogLevel.WARN) { "Not sending events because checkoutAttemptId is null" }
            return
        }

        // TODO: Send correct channel
        val request = analyticsTrackRequestMapper("", eventQueue.takeLast(BATCH_SIZE))

        runSuspendCatching {
            analyticsService.trackEvents(request, checkoutAttemptId, analyticsParams.clientKey)
        }.fold(
            onSuccess = {
                // TODO: Remove events from the queue
            },
            onFailure = {
                // TODO: Handle error
            },
        )

    }

    private fun cannotSendEvent(): Boolean {
        return analyticsParams.level.priority <= AnalyticsParamsLevel.NONE.priority
    }

    companion object {
        private const val CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS = "do-not-track"

        private const val BATCH_SIZE = 20
    }

    @VisibleForTesting
    internal sealed class State {
        data object Uninitialized : State()
        data object InProgress : State()
        data class Ready(val checkoutAttemptId: String) : State()
        data object Failed : State()

        fun canInitialize(): Boolean = when (this) {
            InProgress,
            is Ready -> false

            Failed,
            Uninitialized -> true
        }
    }
}
