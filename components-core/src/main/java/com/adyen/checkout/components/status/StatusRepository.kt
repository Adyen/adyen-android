/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/8/2022.
 */

package com.adyen.checkout.components.status

import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.status.api.StatusResponseUtils
import com.adyen.checkout.components.status.api.StatusService
import com.adyen.checkout.components.status.model.StatusRequest
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.util.runSuspendCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

interface StatusRepository {

    fun poll(paymentData: String): Flow<Result<StatusResponse>>

    fun refreshStatus(paymentData: String)

    fun getMaxPollingDuration(): Long
}

class DefaultStatusRepository constructor(
    private val statusService: StatusService,
    private val clientKey: String,
) : StatusRepository {

    private var delay: Long = 0

    private val refreshFlow: MutableSharedFlow<String> = MutableSingleEventSharedFlow()

    override fun poll(paymentData: String): Flow<Result<StatusResponse>> {
        val startTime = System.currentTimeMillis()

        val pollingFlow = flow {
            while (currentCoroutineContext().isActive) {
                val result = fetchStatus(paymentData)
                emit(result)

                if (result.isSuccess && StatusResponseUtils.isFinalResult(result.getOrThrow()))
                    currentCoroutineContext().cancel()

                if (!updateDelay(startTime)) {
                    emit(Result.failure(IllegalStateException("Max polling time has been exceeded.")))
                    currentCoroutineContext().cancel()
                }

                delay(delay)
            }
        }

        return merge(
            pollingFlow,
            refreshFlow.map { fetchStatus(it) },
        )
    }

    private suspend fun fetchStatus(paymentData: String) = withContext(Dispatchers.IO) {
        runSuspendCatching {
            statusService.checkStatus(clientKey, StatusRequest(paymentData))
        }
    }

    /**
     * @return Returns if the delay time was updated. If not, that means the max polling time has been exceeded.
     */
    private fun updateDelay(startTime: Long): Boolean {
        val elapsedTime = System.currentTimeMillis() - startTime
        return when {
            elapsedTime <= POLLING_THRESHOLD -> {
                delay = POLLING_DELAY_FAST
                true
            }
            elapsedTime <= getMaxPollingDuration() -> {
                delay = POLLING_DELAY_SLOW
                true
            }
            else -> false
        }
    }

    override fun refreshStatus(paymentData: String) {
        refreshFlow.tryEmit(paymentData)
    }

    override fun getMaxPollingDuration(): Long = MAX_POLLING_DURATION_MILLIS

    companion object {
        private val MAX_POLLING_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(15)
        private val POLLING_DELAY_FAST = TimeUnit.SECONDS.toMillis(2)
        private val POLLING_DELAY_SLOW = TimeUnit.SECONDS.toMillis(10)
        private val POLLING_THRESHOLD = TimeUnit.SECONDS.toMillis(60)
    }
}
