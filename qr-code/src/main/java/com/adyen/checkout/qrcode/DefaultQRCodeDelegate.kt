/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode

import android.os.CountDownTimer
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.status.StatusRepository
import com.adyen.checkout.components.status.api.StatusResponseUtils
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
internal class DefaultQRCodeDelegate(
    private val statusRepository: StatusRepository,
) : QRCodeDelegate {

    private val _outputDataFlow = MutableStateFlow<QRCodeOutputData?>(null)
    override val outputDataFlow: Flow<QRCodeOutputData?> = _outputDataFlow

    override val outputData: QRCodeOutputData? get() = _outputDataFlow.value

    private val _exceptionFlow = MutableSharedFlow<CheckoutException>(0, 1, BufferOverflow.DROP_OLDEST)
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private val _detailsFlow = MutableStateFlow<JSONObject?>(null)
    override val detailsFlow: Flow<JSONObject?> = _detailsFlow

    private val _timerFlow = MutableStateFlow(TimerData(0, 0))
    override val timerFlow: Flow<TimerData> = _timerFlow

    private lateinit var coroutineScope: CoroutineScope

    private var statusPollingJob: Job? = null

    private var qrCodeData: String? = null

    private var statusCountDownTimer: CountDownTimer = object : CountDownTimer(
        StatusRepository.MAX_POLLING_DURATION_MILLIS,
        STATUS_POLLING_INTERVAL_MILLIS
    ) {
        override fun onTick(millisUntilFinished: Long) {
            onTimerTick(millisUntilFinished)
        }

        override fun onFinish() = Unit
    }

    private fun onTimerTick(millisUntilFinished: Long) {
        val progressPercentage = (HUNDRED * millisUntilFinished / StatusRepository.MAX_POLLING_DURATION_MILLIS).toInt()
        _timerFlow.tryEmit(TimerData(millisUntilFinished, progressPercentage))
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }

    override fun handleAction(action: QrCodeAction, paymentData: String) {
        qrCodeData = action.qrCodeData

        // Notify UI to get the logo.
        createOutputData(null, action)

        startStatusPolling(paymentData, action)
        statusCountDownTimer.start()
    }

    private fun startStatusPolling(paymentData: String, action: Action) {
        statusPollingJob?.cancel()
        statusPollingJob = statusRepository.poll(paymentData)
            .onEach { onStatus(it, action) }
            .launchIn(coroutineScope)
    }

    private fun onStatus(result: Result<StatusResponse>, action: Action) {
        result.fold(
            onSuccess = { response ->
                Logger.v(TAG, "Status changed - ${response.resultCode}")
                createOutputData(response, action)
                if (StatusResponseUtils.isFinalResult(response)) {
                    onPollingSuccessful(response)
                }
            },
            onFailure = {
                Logger.e(TAG, "Error while polling status", it)
                _exceptionFlow.tryEmit(ComponentException("Error while polling status", it))
            }
        )
    }

    private fun createOutputData(statusResponse: StatusResponse?, action: Action) {
        val isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)
        val outputData = QRCodeOutputData(isValid, action.paymentMethodType)
        _outputDataFlow.tryEmit(outputData)
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        val payload = statusResponse.payload
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
            _detailsFlow.tryEmit(createDetail(payload))
        } else {
            _exceptionFlow.tryEmit(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
        }
    }

    private fun createDetail(payload: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(PAYLOAD_DETAILS_KEY, payload)
        } catch (e: JSONException) {
            _exceptionFlow.tryEmit(ComponentException("Failed to create details.", e))
        }
        return jsonObject
    }

    override fun refreshStatus(paymentData: String) {
        statusRepository.refreshStatus(paymentData)
    }

    override fun getCodeString(): String? = qrCodeData

    override fun onCleared() {
        statusPollingJob?.cancel()
        statusPollingJob = null
        statusCountDownTimer.cancel()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val PAYLOAD_DETAILS_KEY = "payload"
        private val STATUS_POLLING_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(1L)
        private const val HUNDRED = 100
    }
}
