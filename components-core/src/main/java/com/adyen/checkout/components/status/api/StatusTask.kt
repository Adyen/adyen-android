/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.api

import com.adyen.checkout.components.status.model.StatusRequest
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.api.TimeoutTask
import com.adyen.checkout.core.exception.ApiCallException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class StatusTask internal constructor(
    val api: StatusApi,
    logoUrl: String,
    statusRequest: StatusRequest,
    private var callback: StatusCallback?
) : TimeoutTask<StatusResponse?>(
    { StatusService(logoUrl, statusRequest).checkStatus() }
) {

    override fun done() {
        Logger.v(TAG, "done")
        if (isCancelled) {
            Logger.d(TAG, "canceled")
            notifyFailed(ApiCallException("Execution canceled."))
        } else {
            try {
                // timeout just to make sure we don't get stuck, get call is blocking but should be finished or canceled by now.
                val result = get(SAFETY_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                notifySuccess(result!!)
            } catch (e: ExecutionException) {
                Logger.e(TAG, "Execution failed.", e)
                notifyFailed(ApiCallException("Execution failed.", e))
            } catch (e: InterruptedException) {
                Logger.e(TAG, "Execution interrupted.", e)
                notifyFailed(ApiCallException("Execution interrupted.", e))
            } catch (e: TimeoutException) {
                Logger.e(TAG, "Execution timed out.", e)
                notifyFailed(ApiCallException("Execution timed out.", e))
            }
        }
    }

    private fun notifySuccess(statusResponse: StatusResponse) {
        ThreadManager.MAIN_HANDLER.post {
            api.taskFinished()
            callback?.onSuccess(statusResponse)
            callback = null
        }
    }

    private fun notifyFailed(exception: ApiCallException) {
        ThreadManager.MAIN_HANDLER.post {
            api.taskFinished()
            callback?.onFailed(exception)
            callback = null
        }
    }

    /**
     * Interface to receive events on Status task completion.
     */
    interface StatusCallback {
        /**
         * This method will be called on the Main Thread when the Status is received.
         *
         * @param statusResponse The requested status.
         */
        fun onSuccess(statusResponse: StatusResponse)

        /**
         * This method will be called on the Main Thread if there was an error retrieving the Status.
         *
         * @param exception The reason why the call failed.
         */
        fun onFailed(exception: ApiCallException)
    }

    companion object {
        private val TAG = getTag()
        private const val SAFETY_TIMEOUT = 100
    }
}
