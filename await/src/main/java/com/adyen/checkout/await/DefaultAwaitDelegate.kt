/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/8/2022.
 */

package com.adyen.checkout.await

import com.adyen.checkout.components.model.payments.response.Action
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

internal class DefaultAwaitDelegate(
    private val statusRepository: StatusRepository,
) : AwaitDelegate {

    private val _outputDataFlow = MutableStateFlow<AwaitOutputData?>(null)
    override val outputDataFlow: Flow<AwaitOutputData?> = _outputDataFlow

    override val outputData: AwaitOutputData? get() = _outputDataFlow.value

    private val _detailsFlow = MutableStateFlow<JSONObject?>(null)
    override val detailsFlow: Flow<JSONObject?> = _detailsFlow

    private val _exceptionFlow = MutableSharedFlow<CheckoutException>(0, 1, BufferOverflow.DROP_OLDEST)
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var statusPollingJob: Job? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun handleAction(action: Action, paymentData: String) {
        createOutputData(null, action)
        startStatusPolling(paymentData, action)
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
        val outputData = AwaitOutputData(isValid, action.paymentMethodType)
        _outputDataFlow.tryEmit(outputData)
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        // Not authorized status should still call /details so that merchant can get more info
        val payload = statusResponse.payload
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

    override fun onCleared() {
        statusPollingJob?.cancel()
        statusPollingJob = null
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val PAYLOAD_DETAILS_KEY = "payload"
    }
}
