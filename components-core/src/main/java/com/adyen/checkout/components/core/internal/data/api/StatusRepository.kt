/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/8/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.StatusRequest
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StatusRepository {

    fun poll(paymentData: String, maxPollingDuration: Long): Flow<Result<StatusResponse>>

    fun refreshStatus(paymentData: String)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultStatusRepository constructor(
    private val statusService: StatusService,
    private val clientKey: String,
) : StatusRepository {

    private var delay: Long = 0

    private val refreshFlow: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)

    override fun poll(paymentData: String, maxPollingDuration: Long): Flow<Result<StatusResponse>> {
        val startTime = System.currentTimeMillis()

        val pollingFlow = flow {
            while (currentCoroutineContext().isActive) {
                val result = fetchStatus(paymentData)
                emit(result)

                if (result.isSuccess && StatusResponseUtils.isFinalResult(result.getOrThrow())) {
                    currentCoroutineContext().cancel()
                }

                if (!updateDelay(startTime, maxPollingDuration)) {
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
    private fun updateDelay(startTime: Long, maxPollingDuration: Long): Boolean {
        val elapsedTime = System.currentTimeMillis() - startTime
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
        Logger.v(TAG, "refreshStatus")
        refreshFlow.tryEmit(paymentData)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private val POLLING_DELAY_FAST = TimeUnit.SECONDS.toMillis(2)
        private val POLLING_DELAY_SLOW = TimeUnit.SECONDS.toMillis(10)
        private val POLLING_THRESHOLD = TimeUnit.SECONDS.toMillis(60)
    }
}
