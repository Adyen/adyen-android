/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.internal.analytics.data.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class DefaultAnalyticsManager(
    private val analyticsRepository: AnalyticsRepository,
    private val analyticsParams: AnalyticsParams,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AnalyticsManager {

    private var checkoutAttemptId: String? = null

    private var isInitialized: Boolean = false

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var timerJob: Job? = null

    private var ownerReference: String? = null

    override fun initialize(owner: Any, coroutineScope: CoroutineScope) {
        if (isInitialized) {
            adyenLog(AdyenLogLevel.DEBUG) { "Already initialized, ignoring." }
            return
        }
        isInitialized = true

        ownerReference = owner::class.qualifiedName
        _coroutineScope = coroutineScope

        coroutineScope.launch(coroutineDispatcher) {
            runSuspendCatching {
                analyticsRepository.fetchCheckoutAttemptId()
            }.fold(
                onSuccess = { attemptId ->
                    checkoutAttemptId = attemptId?.also { startTimer() }
                },
                onFailure = {
                    adyenLog(AdyenLogLevel.WARN, it) { "Failed to fetch checkoutAttemptId." }
                    checkoutAttemptId = FAILED_CHECKOUT_ATTEMPT_ID
                },
            )
        }
    }

    override fun trackEvent(event: AnalyticsEvent) {
        if (cannotSendEvents()) {
            adyenLog(AdyenLogLevel.DEBUG) { "Not allowed to track events, ignoring." }
            return
        }
        coroutineScope.launch(coroutineDispatcher) {
            runSuspendCatching {
                analyticsRepository.storeEvent(event)

                if (event.shouldForceSend) {
                    sendEvents()
                    startTimer()
                }
            }.fold(
                onSuccess = { /* Not necessary */ },
                onFailure = { throwable -> adyenLog(AdyenLogLevel.WARN, throwable) { "Storing event failed" } },
            )
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = coroutineScope.launch(coroutineDispatcher) {
            while (isActive) {
                delay(DISPATCH_INTERVAL_MILLIS)
                sendEvents()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private suspend fun sendEvents() {
        val checkoutAttemptId = checkoutAttemptId
        if (checkoutAttemptId == null) {
            adyenLog(AdyenLogLevel.WARN) { "checkoutAttemptId should not be null at this point." }
            return
        }

        if (cannotSendEvents()) {
            adyenLog(AdyenLogLevel.DEBUG) { "Not allowed to send events, ignoring." }
            return
        }

        runSuspendCatching {
            analyticsRepository.sendEvents(checkoutAttemptId)
        }.fold(
            onSuccess = { /* Not necessary */ },
            onFailure = { throwable -> adyenLog(AdyenLogLevel.WARN, throwable) { "Failed sending analytics events" } },
        )
    }

    override fun getCheckoutAttemptId(): String? = checkoutAttemptId

    private fun cannotSendEvents(): Boolean {
        return analyticsParams.level.priority <= AnalyticsParamsLevel.NONE.priority ||
            checkoutAttemptId == FAILED_CHECKOUT_ATTEMPT_ID
    }

    override fun clear(owner: Any) {
        if (ownerReference != owner::class.qualifiedName) {
            adyenLog(AdyenLogLevel.DEBUG) { "Clear called by not the original owner, ignoring." }
            return
        }

        adyenLog(AdyenLogLevel.DEBUG) { "Clearing analytics manager" }

        _coroutineScope = null
        checkoutAttemptId = null
        ownerReference = null
        isInitialized = false
        stopTimer()
        timerJob = null
    }

    companion object {
        @VisibleForTesting
        internal val FAILED_CHECKOUT_ATTEMPT_ID = "fetch-checkoutAttemptId-failed"

        @VisibleForTesting
        internal val DISPATCH_INTERVAL_MILLIS = 10.seconds.inWholeMilliseconds
    }
}
