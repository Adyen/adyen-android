/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.analytics.internal.data.AnalyticsRepository
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class DefaultAnalyticsManager(
    private val analyticsRepository: AnalyticsRepository,
    private val analyticsParams: AnalyticsParams,
    private val checkoutAttemptId: String,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) : AnalyticsManager {

    private var isInitialized: Boolean = false

    private var coroutineScope: CoroutineScope? = null

    private var timerJob: Job? = null

    private var ownerReference: String? = null

    override fun initialize(owner: Any, coroutineScope: CoroutineScope) {
        if (isInitialized) {
            adyenLog(AdyenLogLevel.DEBUG) { "Already initialized, ignoring." }
            return
        }
        isInitialized = true

        ownerReference = owner::class.qualifiedName
        this.coroutineScope = coroutineScope

        coroutineScope.launch(coroutineDispatcher) {
            runSuspendCatching {
                analyticsRepository.fetchCheckoutAttemptId()
            }.fold(
                onSuccess = {
                    startTimer()
                },
                onFailure = {
                    adyenLog(AdyenLogLevel.WARN, it) { "Failed to fetch checkoutAttemptId." }
                },
            )
        }
    }

    override fun trackEvent(event: AnalyticsEvent) {
        if (cannotSendEvents()) {
            adyenLog(AdyenLogLevel.DEBUG) { "Not allowed to track events, ignoring." }
            return
        }
        coroutineScope?.launch(coroutineDispatcher) {
            runSuspendCatching {
                analyticsRepository.storeEvent(event)

                if (event.shouldForceSend) {
                    sendEvents()
                    startTimer()
                }
            }.fold(
                onSuccess = { /* Not necessary */ },
                onFailure = { throwable ->
                    adyenLog(AdyenLogLevel.WARN, throwable) { "Storing event failed" }
                },
            )
        } ?: adyenLog(AdyenLogLevel.WARN) { "Coroutine scope is null. Tracking event failed." }
    }

    private fun startTimer() {
        stopTimer()
        if (coroutineScope == null) {
            adyenLog(AdyenLogLevel.WARN) { "Coroutine scope is null." }
            return
        }
        timerJob = coroutineScope?.launch(coroutineDispatcher) {
            while (isActive) {
                delay(DISPATCH_INTERVAL_SECONDS)
                sendEvents()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private suspend fun sendEvents() {
        runSuspendCatching {
            analyticsRepository.sendEvents(checkoutAttemptId)
        }.fold(
            onSuccess = { /* Not necessary */ },
            onFailure = { throwable ->
                adyenLog(AdyenLogLevel.WARN, throwable) { "Failed sending analytics events" }
            },
        )
    }

    override fun getCheckoutAttemptId(): String = checkoutAttemptId

    private fun cannotSendEvents() = analyticsParams.level.priority <= AnalyticsParamsLevel.INITIAL.priority

    override fun clear(owner: Any) {
        if (ownerReference != owner::class.qualifiedName) {
            adyenLog(AdyenLogLevel.DEBUG) { "Clear called by not the original owner, ignoring." }
            return
        }

        adyenLog(AdyenLogLevel.DEBUG) { "Clearing analytics manager" }

        coroutineScope = null
        ownerReference = null
        isInitialized = false
        stopTimer()
        timerJob = null
    }

    companion object {

        @VisibleForTesting
        internal val DISPATCH_INTERVAL_SECONDS = 10.seconds
    }
}
