/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.await

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.BaseLifecycleObserver
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.status.StatusRepository
import com.adyen.checkout.components.status.StatusRepository.Companion.getInstance
import com.adyen.checkout.components.status.api.StatusResponseUtils.isFinalResult
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import org.json.JSONObject

class AwaitComponent(savedStateHandle: SavedStateHandle, application: Application, configuration: AwaitConfiguration) :
    BaseActionComponent<AwaitConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<AwaitOutputData, AwaitConfiguration, ActionComponentData> {

    val statusRepository: StatusRepository = getInstance(configuration.environment)
    private val outputLiveData = MutableLiveData<AwaitOutputData>()
    private var paymentMethodType: String? = null

    private val responseObserver: Observer<StatusResponse?> = Observer { statusResponse ->
        Logger.v(TAG, "onChanged - ${if (statusResponse == null) "null" else statusResponse.resultCode}")
        createOutputData(statusResponse)
        if (statusResponse != null && isFinalResult(statusResponse)) {
            onPollingSuccessful(statusResponse)
        }
    }
    private val errorObserver: Observer<ComponentException?> = Observer { e ->
        // StatusRepository will post null errors to reset it's status. We can ignore.
        if (e != null) {
            Logger.e(TAG, "onError")
            notifyException(e)
        }
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        val configuration: Configuration = configuration
        paymentMethodType = action.paymentMethodType
        // Notify UI to get the logo.
        createOutputData(null)
        paymentData?.let {
            statusRepository.startPolling(configuration.clientKey, it)
        } ?: throw ComponentException("paymentData is null")
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)
        statusRepository.responseLiveData.observe(lifecycleOwner, responseObserver)
        statusRepository.errorLiveData.observe(lifecycleOwner, errorObserver)

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : BaseLifecycleObserver() {
            override fun onResume() {
                statusRepository.updateStatus()
            }
        })
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        // Not authorized status should still call /details so that merchant can get more info
        val payload = statusResponse.payload
        if (isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
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

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<AwaitOutputData>) {
        outputLiveData.observe(lifecycleOwner, observer)
    }

    override val outputData: AwaitOutputData?
        get() = outputLiveData.value

    override fun sendAnalyticsEvent(context: Context) {
        // noop
    }

    private fun createOutputData(statusResponse: StatusResponse?) {
        val isValid = statusResponse != null && isFinalResult(statusResponse)
        val outputData = AwaitOutputData(isValid, paymentMethodType)
        outputLiveData.value = outputData
    }

    companion object {
        val TAG = getTag()
        private const val PAYLOAD_DETAILS_KEY = "payload"
        @JvmField
        val PROVIDER: ActionComponentProvider<AwaitComponent, AwaitConfiguration> = AwaitComponentProvider()
    }
}
