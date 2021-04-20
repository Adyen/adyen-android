/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */
package com.adyen.checkout.qrcode

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.ActionComponentProviderImpl
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.lifecycle.BaseLifecycleObserver
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.status.StatusRepository
import com.adyen.checkout.components.status.api.StatusResponseUtils
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val TAG = LogUtil.getTag()
private val ACTION_TYPES = listOf(QrCodeAction.ACTION_TYPE)
private val PAYMENT_METHODS = listOf(PaymentMethodTypes.PIX)
private const val PAYLOAD_DETAILS_KEY = "payload"
private val STATUS_POLLING_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(1L) // 1 second
private const val HUNDRED = 100

@Suppress("TooManyFunctions")
class QRCodeComponent(application: Application, configuration: QRCodeConfiguration) :
    BaseActionComponent<QRCodeConfiguration>(application, configuration),
    ViewableComponent<QRCodeOutputData, QRCodeConfiguration, ActionComponentData> {

    private val outputLiveData = MutableLiveData<QRCodeOutputData>()
    private var paymentMethodType: String? = null
    private var qrCodeData: String? = null
    private val statusRepository: StatusRepository = StatusRepository.getInstance(configuration.environment)
    private val timerLiveData = MutableLiveData<TimerData>()
    private var statusCountDownTimer: CountDownTimer = object : CountDownTimer(
        statusRepository.maxPollingDurationMillis,
        STATUS_POLLING_INTERVAL_MILLIS
    ) {
        override fun onTick(millisUntilFinished: Long) {
            onTimerTick(millisUntilFinished)
        }

        override fun onFinish() {
            // do nothing, StatusRepository will finish the polling automatically
        }
    }

    private val responseObserver: Observer<StatusResponse> = Observer { statusResponse ->
        Logger.v(TAG, "onChanged - " + if (statusResponse == null) "null" else statusResponse.resultCode)
        createOutputData(statusResponse)
        if (statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)) {
            onPollingSuccessful(statusResponse)
        }
    }
    private val mErrorObserver: Observer<ComponentException> = Observer { e ->
        // StatusRepository will post null errors to reset it's status. We can ignore.
        if (e != null) {
            Logger.e(TAG, "onError")
            notifyException(e)
        }
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        val configuration = configuration
            ?: throw ComponentException("Configuration not found")
        if (action !is QrCodeAction) throw ComponentException("Unsupported action")
        paymentMethodType = action.paymentMethodType
        qrCodeData = action.qrCodeData
        // Notify UI to get the logo.
        createOutputData(null)
        val data = paymentData ?: return
        statusRepository.startPolling(configuration.clientKey, data)
        statusCountDownTimer.start()
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)
        statusRepository.statusResponseLiveData.observe(lifecycleOwner, responseObserver)
        statusRepository.errorLiveData.observe(lifecycleOwner, mErrorObserver)

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : BaseLifecycleObserver() {
            override fun onResume() {
                statusRepository.updateStatus()
            }
        })
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        val payload = statusResponse.payload
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
            notifyDetails(createDetail(payload))
        } else {
            notifyException(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
        }
    }

    private fun createDetail(payload: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(PAYLOAD_DETAILS_KEY, payload)
        } catch (e: JSONException) {
            notifyException(ComponentException("Failed to create details.", e))
        }
        return jsonObject
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        statusRepository.stopPolling()
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<QRCodeOutputData>) {
        outputLiveData.observe(lifecycleOwner, observer)
    }

    fun observeTimer(lifecycleOwner: LifecycleOwner, observer: Observer<TimerData>) {
        timerLiveData.observe(lifecycleOwner, observer)
    }

    override fun getOutputData(): QRCodeOutputData? {
        return outputLiveData.value
    }

    override fun sendAnalyticsEvent(context: Context) {
        // noop
    }

    private fun createOutputData(statusResponse: StatusResponse?) {
        val isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)
        val outputData = QRCodeOutputData(isValid, paymentMethodType)
        outputLiveData.value = outputData
    }

    fun getCodeString(): String? {
        return qrCodeData
    }

    private fun onTimerTick(millisUntilFinished: Long) {
        val progressPercentage = (HUNDRED * millisUntilFinished / statusRepository.maxPollingDurationMillis).toInt()
        timerLiveData.postValue(TimerData(millisUntilFinished, progressPercentage))
    }

    override fun getSupportedActionTypes(): List<String> = ACTION_TYPES

    override fun getSupportedPaymentMethodTypes(): List<String>  = PAYMENT_METHODS

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<QRCodeComponent, QRCodeConfiguration> = ActionComponentProviderImpl(
            QRCodeComponent::class.java,
            QRCodeConfiguration::class.java,
            true
        )
    }
}

data class TimerData(
    val millisUntilFinished: Long,
    val progress: Int
)
