/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/8/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.internal.data.model.StatusRequest
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StatusRepository {

    fun poll(paymentData: String, maxPollingDuration: Long): Flow<Result<StatusResponse>>

    fun refreshStatus(paymentData: String)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultStatusRepository(
    private val statusService: StatusService,
    private val clientKey: String,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) : StatusRepository {

    private var delay: Long = 0

    private val refreshFlow = bufferedChannel<String>()

    @OptIn(FlowPreview::class)
    override fun poll(paymentData: String, maxPollingDuration: Long): Flow<Result<StatusResponse>> {
        val startTime = timeSource.markNow()

        updateDelay(startTime, maxPollingDuration)

        val pollingFlow = flow {
            while (currentCoroutineContext().isActive) {
                emit(paymentData)
                delay(delay)
            }
        }

        return merge(
            pollingFlow,
            refreshFlow.receiveAsFlow(),
        )
            .debounce(DEBOUNCE_TIME)
            .map {
                fetchStatus(it)
            }
            .filterNot {
                it.exceptionOrNull() is UnknownHostException
            }
            .transform { result ->
                emit(result)

                if (result.isSuccess && StatusResponseUtils.isFinalResult(result.getOrThrow())) {
                    currentCoroutineContext().cancel()
                }

                if (!updateDelay(startTime, maxPollingDuration)) {
                    adyenLog(AdyenLogLevel.DEBUG) { "Max polling time exceeded" }
                    emit(Result.failure(IllegalStateException("Max polling time exceeded.")))
                    currentCoroutineContext().cancel()
                }
            }
            .onEach {
                adyenLog(AdyenLogLevel.DEBUG) { "Emitting status: ${it.getOrNull()?.resultCode}" }
            }
    }

    private suspend fun fetchStatus(paymentData: String) = withContext(coroutineDispatcher) {
        runSuspendCatching {
            statusService.checkStatus(clientKey, StatusRequest(paymentData))
        }
    }

    /**
     * @return Returns if the delay time was updated. If not, that means the max polling time has been exceeded.
     */
    private fun updateDelay(startTime: TimeMark, maxPollingDuration: Long): Boolean {
        val elapsedTime = startTime.elapsedNow().inWholeMilliseconds
        return when {
            elapsedTime <= POLLING_THRESHOLD -> {
                delay = POLLING_DELAY_FAST
                true
            }

            elapsedTime <= maxPollingDuration -> {
                delay = POLLING_DELAY_SLOW
                true
            }

            else -> false
        }
    }

    override fun refreshStatus(paymentData: String) {
        adyenLog(AdyenLogLevel.VERBOSE) { "refreshStatus" }
        refreshFlow.trySend(paymentData)
    }

    companion object {
        private val POLLING_DELAY_FAST = 2.seconds.inWholeMilliseconds
        private val POLLING_DELAY_SLOW = 10.seconds.inWholeMilliseconds
        private val POLLING_THRESHOLD = 60.seconds.inWholeMilliseconds

        @VisibleForTesting
        internal val DEBOUNCE_TIME = 100.milliseconds.inWholeMilliseconds
    }
}
