/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.api

import com.adyen.checkout.components.status.model.StatusRequest
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.exception.ApiCallException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

class StatusApi private constructor(host: String) {
    private val statusUrlFormat: String

    init {
        Logger.v(TAG, "Environment URL - $host")
        statusUrlFormat = host + STATUS_PATH
    }

    // We will only handle 1 call at a time.
    private var mCurrentTask: StatusConnectionTask? = null
    fun taskFinished() {
        synchronized(this) { mCurrentTask = null }
    }

    /**
     * Starts a request to to the Status endpoint.
     *
     * @param clientKey The clientKey that identifies the merchant.
     * @param paymentData The paymentData that identifies the payment.
     * @param callback The callback to receive the result.
     */
    fun callStatus(clientKey: String, paymentData: String, callback: StatusConnectionTask.StatusCallback) {
        Logger.v(TAG, "getStatus")
        val url = String.format(statusUrlFormat, clientKey)
        synchronized(this) {
            if (mCurrentTask != null) {
                Logger.e(TAG, "Status already pending.")
                callback.onFailed(ApiCallException("Other Status call already pending."))
            }
            val statusRequest = StatusRequest()
            statusRequest.paymentData = paymentData
            mCurrentTask = StatusConnectionTask(this, url, statusRequest, callback)
            ThreadManager.EXECUTOR.submit(mCurrentTask)
        }
    }

    companion object {
        private val TAG = getTag()

        // %1$s = client key
        private const val STATUS_PATH = "services/PaymentInitiation/v1/status?token=%1\$s"
        private var sInstance: StatusApi? = null

        /**
         * Get the instance of the [StatusApi] for the specified environment.
         *
         * @param environment The URL of the server for making the calls. Should be the same used in the Payment.
         * @return The instance of the [StatusApi].
         */
        @JvmStatic
        fun getInstance(environment: Environment): StatusApi {
            val hostUrl = environment.baseUrl
            synchronized(StatusApi::class.java) {
                if (sInstance == null || isDifferentHost(sInstance!!, hostUrl)) {
                    sInstance = StatusApi(hostUrl)
                }
                return sInstance!!
            }
        }

        private fun isDifferentHost(statusApi: StatusApi, hostUrl: String): Boolean {
            return !statusApi.statusUrlFormat.startsWith(hostUrl)
        }
    }
}
