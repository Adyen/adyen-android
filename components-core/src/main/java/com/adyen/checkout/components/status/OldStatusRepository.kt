/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.components.status

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.components.status.api.StatusApi
import com.adyen.checkout.components.status.api.StatusResponseUtils.isFinalResult
import com.adyen.checkout.components.status.api.StatusTask
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ApiCallException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger
import java.util.concurrent.TimeUnit

class OldStatusRepository private constructor(environment: Environment) {

    val handler = Handler(Looper.getMainLooper())
    private val statusPollingRunnable: Runnable = object : Runnable {
        override fun run() {
            Logger.d(TAG, "mStatusPollingRunnable.run()")
            statusApi.callStatus(clientKey!!, paymentData!!, statusCallback)
            updatePollingDelay()
            handler.postDelayed(this, pollingDelay)
        }
    }

    val statusApi: StatusApi = StatusApi.getInstance(environment)
    private val _responseLiveData = MutableLiveData<StatusResponse?>()
    val responseLiveData: LiveData<StatusResponse?> = _responseLiveData
    private val _errorLiveData = MutableLiveData<ComponentException?>()
    val errorLiveData: LiveData<ComponentException?> = _errorLiveData

    val statusCallback: StatusTask.StatusCallback = object : StatusTask.StatusCallback {
        override fun onSuccess(statusResponse: StatusResponse) {
            Logger.d(TAG, "onSuccess - " + statusResponse.resultCode)
            _responseLiveData.postValue(statusResponse)
            if (isFinalResult(statusResponse)) {
                stopPolling()
            }
        }

        override fun onFailed(exception: ApiCallException) {
            Logger.e(TAG, "onFailed")
            // TODO: 08/09/2020 check error type, fail flow if no internet?
        }
    }

    var clientKey: String? = null
    var paymentData: String? = null
    var pollingDelay: Long = 0
    private var isPolling = false
    private var pollingStartTime: Long = 0

    /**
     * Start polling status requests for the provided payment.
     *
     * @param clientKey The client key that identifies the merchant.
     * @param paymentData The payment data of the payment we are requesting.
     */
    fun startPolling(clientKey: String, paymentData: String) {
        Logger.d(TAG, "startPolling")
        if (isPolling && clientKey == this.clientKey && paymentData == this.paymentData) {
            Logger.e(TAG, "Already polling for this payment.")
            return
        }
        stopPolling()
        isPolling = true
        this.clientKey = clientKey
        this.paymentData = paymentData
        pollingStartTime = System.currentTimeMillis()
        handler.post(statusPollingRunnable)
    }

    /**
     * Immediately request a status update instead of waiting for the next poll result.
     */
    fun updateStatus() {
        Logger.d(TAG, "updateStatus")
        if (!isPolling) {
            Logger.d(TAG, "No polling in progress")
            return
        }
        handler.removeCallbacks(statusPollingRunnable)
        handler.post(statusPollingRunnable)
    }

    /**
     * Stops the polling process.
     */
    fun stopPolling() {
        Logger.d(TAG, "stopPolling")
        if (!isPolling) {
            Logger.w(TAG, "Stop called with no polling in progress, stopping anyway")
        }
        isPolling = false
        handler.removeCallbacksAndMessages(null)
        // Set null so that new observers don't get the status from the previous result
        // This could be replaced by other types of observable like Kotlin Flow
        _responseLiveData.value = null
        _errorLiveData.value = null
    }

    fun updatePollingDelay() {
        val elapsedTime = System.currentTimeMillis() - pollingStartTime
        when {
            elapsedTime <= POLLING_THRESHOLD -> pollingDelay = POLLING_DELAY_FAST
            elapsedTime <= MAX_POLLING_DURATION_MILLIS -> pollingDelay = POLLING_DELAY_SLOW
            else -> _errorLiveData.setValue(ComponentException("Status requesting timed out with no result"))
        }
    }

    companion object {
        val TAG = getTag()
        val MAX_POLLING_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(15)

        private val POLLING_DELAY_FAST = TimeUnit.SECONDS.toMillis(2)
        private val POLLING_DELAY_SLOW = TimeUnit.SECONDS.toMillis(10)
        private val POLLING_THRESHOLD = TimeUnit.SECONDS.toMillis(60)

        private lateinit var instance: OldStatusRepository

        @JvmStatic
        fun getInstance(environment: Environment): OldStatusRepository {
            synchronized(OldStatusRepository::class.java) {
                if (!::instance.isInitialized) {
                    instance = OldStatusRepository(environment)
                }
            }
            return instance
        }
    }
}
