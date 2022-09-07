/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.StatusRepository
import com.adyen.checkout.components.status.api.StatusResponseUtils
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.handler.RedirectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    private val statusCountDownTimer: QRCodeCountDownTimer,
    private val redirectHandler: RedirectHandler,
    private val paymentDataRepository: PaymentDataRepository,
) : QRCodeDelegate {

    private val _outputDataFlow = MutableStateFlow<QRCodeOutputData?>(null)
    override val outputDataFlow: Flow<QRCodeOutputData?> = _outputDataFlow

    override val outputData: QRCodeOutputData? get() = _outputDataFlow.value

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private val _detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSingleEventSharedFlow()
    override val detailsFlow: Flow<ActionComponentData> = _detailsFlow

    private val _timerFlow = MutableStateFlow(TimerData(0, 0))
    override val timerFlow: Flow<TimerData> = _timerFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var statusPollingJob: Job? = null

    init {
        statusCountDownTimer.attach(
            millisInFuture = statusRepository.getMaxPollingDuration(),
            countDownInterval = STATUS_POLLING_INTERVAL_MILLIS
        ) { millisUntilFinished -> onTimerTick(millisUntilFinished) }
    }

    @VisibleForTesting
    internal fun onTimerTick(millisUntilFinished: Long) {
        val progressPercentage =
            (HUNDRED * millisUntilFinished / statusRepository.getMaxPollingDuration()).toInt()
        _timerFlow.tryEmit(TimerData(millisUntilFinished, progressPercentage))
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun handleAction(action: QrCodeAction, activity: Activity) {
        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        if (paymentData == null) {
            Logger.e(TAG, "Payment data is null")
            _exceptionFlow.tryEmit(ComponentException("Payment data is null"))
            return
        }

        if (!requiresView(action)) {
            Logger.d(TAG, "Action does not require a view, redirecting.")
            makeRedirect(activity, action)
            return
        }

        // Notify UI to get the logo.
        createOutputData(null, action)

        startStatusPolling(paymentData, action)
        statusCountDownTimer.start()
    }

    private fun makeRedirect(activity: Activity, action: QrCodeAction) {
        val url = action.url
        try {
            Logger.d(TAG, "makeRedirect - $url")
            redirectHandler.launchUriRedirect(activity, url)
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }

    private fun startStatusPolling(paymentData: String, action: QrCodeAction) {
        statusPollingJob?.cancel()
        statusPollingJob = statusRepository.poll(paymentData)
            .onEach { onStatus(it, action) }
            .launchIn(coroutineScope)
    }

    private fun onStatus(result: Result<StatusResponse>, action: QrCodeAction) {
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

    private fun createOutputData(statusResponse: StatusResponse?, action: QrCodeAction) {
        val isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)
        val outputData = QRCodeOutputData(isValid, action.paymentMethodType, action.qrCodeData)
        _outputDataFlow.tryEmit(outputData)
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        val payload = statusResponse.payload
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
            val details = createDetails(payload)
            _detailsFlow.tryEmit(createActionComponentData(details))
        } else {
            _exceptionFlow.tryEmit(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
        }
    }

    private fun requiresView(action: QrCodeAction): Boolean {
        return VIEWABLE_PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun refreshStatus() {
        val paymentData = paymentDataRepository.paymentData ?: return
        statusRepository.refreshStatus(paymentData)
    }

    override fun handleIntent(intent: Intent) {
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
            _detailsFlow.tryEmit(createActionComponentData(details))
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }

    private fun createDetails(payload: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(PAYLOAD_DETAILS_KEY, payload)
        } catch (e: JSONException) {
            _exceptionFlow.tryEmit(ComponentException("Failed to create details.", e))
        }
        return jsonObject
    }

    override fun onCleared() {
        statusPollingJob?.cancel()
        statusPollingJob = null
        statusCountDownTimer.cancel()
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @VisibleForTesting
        internal const val PAYLOAD_DETAILS_KEY = "payload"
        private val STATUS_POLLING_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(1L)
        private const val HUNDRED = 100

        private val VIEWABLE_PAYMENT_METHODS = listOf(PaymentMethodTypes.PIX)
    }
}
