/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.qrcode.internal.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.StatusResponseUtils
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.data.api.StatusRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.repeatOnResume
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.FileDownloader
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.qrcode.internal.QRCodeCountDownTimer
import com.adyen.checkout.qrcode.internal.ui.model.QRCodeOutputData
import com.adyen.checkout.qrcode.internal.ui.model.QrCodeUIEvent
import com.adyen.checkout.ui.core.internal.RedirectHandler
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultQRCodeDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val statusRepository: StatusRepository,
    private val statusCountDownTimer: QRCodeCountDownTimer,
    private val redirectHandler: RedirectHandler,
    private val paymentDataRepository: PaymentDataRepository,
    private val fileDownloader: FileDownloader
) : QRCodeDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<QRCodeOutputData> = _outputDataFlow

    override val outputData: QRCodeOutputData get() = _outputDataFlow.value

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val _timerFlow = MutableStateFlow(TimerData(0, 0))
    override val timerFlow: Flow<TimerData> = _timerFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val eventChannel: Channel<QrCodeUIEvent> = bufferedChannel()
    override val eventFlow: Flow<QrCodeUIEvent> = eventChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var statusPollingJob: Job? = null

    private var currentAction: Action? = null

    private var maxPollingDurationMillis = DEFAULT_MAX_POLLING_DURATION

    private fun attachStatusTimer() {
        statusCountDownTimer.attach(
            millisInFuture = maxPollingDurationMillis,
            countDownInterval = STATUS_POLLING_INTERVAL_MILLIS
        ) { millisUntilFinished -> onTimerTick(millisUntilFinished) }
    }

    @VisibleForTesting
    internal fun onTimerTick(millisUntilFinished: Long) {
        val progressPercentage =
            (HUNDRED * millisUntilFinished / maxPollingDurationMillis).toInt()
        _timerFlow.tryEmit(TimerData(millisUntilFinished, progressPercentage))
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.repeatOnResume { refreshStatus() }
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    @Suppress("ReturnCount")
    override fun handleAction(action: Action, activity: Activity) {
        if (action !is QrCodeAction) {
            exceptionChannel.trySend(ComponentException("Unsupported action"))
            return
        }

        currentAction = action

        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        if (paymentData == null) {
            Logger.e(TAG, "Payment data is null")
            exceptionChannel.trySend(ComponentException("Payment data is null"))
            return
        }

        if (shouldLaunchRedirect(action)) {
            Logger.d(TAG, "Action does not require a view, redirecting.")
            _viewFlow.tryEmit(QrCodeComponentViewType.REDIRECT)
            makeRedirect(activity, action)
            return
        }

        val viewType = when (action.paymentMethodType) {
            PaymentMethodTypes.PAY_NOW -> {
                maxPollingDurationMillis = PAY_NOW_MAX_POLLING_DURATION
                QrCodeComponentViewType.FULL_QR_CODE
            }
            PaymentMethodTypes.UPI_QR -> {
                maxPollingDurationMillis = UPI_MAX_POLLING_DURATION
                QrCodeComponentViewType.FULL_QR_CODE
            }
            else -> {
                maxPollingDurationMillis = DEFAULT_MAX_POLLING_DURATION
                QrCodeComponentViewType.SIMPLE_QR_CODE
            }
        }
        _viewFlow.tryEmit(viewType)

        // Notify UI to get the logo.
        createOutputData(null, action)

        attachStatusTimer()
        startStatusPolling(paymentData, action)
        statusCountDownTimer.start()
    }

    private fun makeRedirect(activity: Activity, action: QrCodeAction) {
        val url = action.url
        try {
            Logger.d(TAG, "makeRedirect - $url")
            redirectHandler.launchUriRedirect(activity, url)
        } catch (ex: CheckoutException) {
            exceptionChannel.trySend(ex)
        }
    }

    private fun startStatusPolling(paymentData: String, action: QrCodeAction) {
        statusPollingJob?.cancel()
        statusPollingJob = statusRepository.poll(paymentData, maxPollingDurationMillis)
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
                exceptionChannel.trySend(ComponentException("Error while polling status", it))
            }
        )
    }

    private fun createOutputData(statusResponse: StatusResponse?, action: QrCodeAction) {
        val isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)

        var qrImageUrl: String? = null
        if (_viewFlow.value == QrCodeComponentViewType.FULL_QR_CODE) {
            val encodedQrCodeData = Uri.encode(action.qrCodeData)
            qrImageUrl = String.format(QR_IMAGE_BASE_PATH, componentParams.environment.baseUrl, encodedQrCodeData)
        }

        val outputData = QRCodeOutputData(
            isValid = isValid,
            paymentMethodType = action.paymentMethodType,
            qrCodeData = action.qrCodeData,
            qrImageUrl = qrImageUrl
        )
        _outputDataFlow.tryEmit(outputData)
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        val payload = statusResponse.payload
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
            val details = createDetails(payload)
            detailsChannel.trySend(createActionComponentData(details))
        } else {
            exceptionChannel.trySend(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
        }
    }

    private fun shouldLaunchRedirect(action: QrCodeAction): Boolean {
        return !VIEWABLE_PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun refreshStatus() {
        val paymentData = paymentDataRepository.paymentData ?: return
        statusRepository.refreshStatus(paymentData)
    }

    override fun handleIntent(intent: Intent) {
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
            detailsChannel.trySend(createActionComponentData(details))
        } catch (ex: CheckoutException) {
            exceptionChannel.trySend(ex)
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
            exceptionChannel.trySend(ComponentException("Failed to create details.", e))
        }
        return jsonObject
    }

    override fun onError(e: CheckoutException) {
        exceptionChannel.trySend(e)
    }

    override fun onCleared() {
        removeObserver()
        statusPollingJob?.cancel()
        statusPollingJob = null
        statusCountDownTimer.cancel()
        _coroutineScope = null
    }

    private fun createOutputData() = QRCodeOutputData(
        isValid = false,
        paymentMethodType = null,
        qrCodeData = null
    )

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun downloadQRImage() {
        val date: Long = System.currentTimeMillis()
        val imageName = String.format(IMAGE_NAME_FORMAT, currentAction?.paymentMethodType, date)
        val imageDirectory = android.os.Environment.DIRECTORY_DOWNLOADS.orEmpty()
        coroutineScope.launch {
            fileDownloader.download(
                outputData.qrImageUrl.orEmpty(),
                imageName,
                imageDirectory,
                MIME_TYPE
            ).fold(
                onSuccess = { eventChannel.trySend(QrCodeUIEvent.QrImageDownloadResult.Success) },
                onFailure = { e -> eventChannel.trySend(QrCodeUIEvent.QrImageDownloadResult.Failure(e)) }
            )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private val VIEWABLE_PAYMENT_METHODS = listOf(
            PaymentMethodTypes.PIX,
            PaymentMethodTypes.PAY_NOW,
            PaymentMethodTypes.UPI_QR,
        )

        @VisibleForTesting
        internal const val PAYLOAD_DETAILS_KEY = "payload"
        private val STATUS_POLLING_INTERVAL_MILLIS = 1.seconds.inWholeMilliseconds
        private val PAY_NOW_MAX_POLLING_DURATION = 3.minutes.inWholeMilliseconds
        private val UPI_MAX_POLLING_DURATION = 5.minutes.inWholeMilliseconds
        private val DEFAULT_MAX_POLLING_DURATION = 15.minutes.inWholeMilliseconds
        private const val HUNDRED = 100

        private const val IMAGE_NAME_FORMAT = "%s-%s.png"
        private const val QR_IMAGE_BASE_PATH = "%sbarcode.shtml?barcodeType=qrCode&fileType=png&data=%s"
        private const val MIME_TYPE = "image/png"
    }
}
