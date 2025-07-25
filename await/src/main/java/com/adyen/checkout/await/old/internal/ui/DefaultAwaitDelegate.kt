/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/8/2022.
 */

package com.adyen.checkout.await.old.internal.ui

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.await.old.internal.ui.model.AwaitOutputData
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.StatusRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.repeatOnResume
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.old.internal.RedirectHandler
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Suppress("LongParameterList", "TooManyFunctions")
internal class DefaultAwaitDelegate(
    private val observerRepository: ActionObserverRepository,
    override val savedStateHandle: SavedStateHandle,
    override val componentParams: GenericComponentParams,
    private val redirectHandler: RedirectHandler,
    private val statusRepository: StatusRepository,
    private val paymentDataRepository: PaymentDataRepository,
    private val analyticsManager: AnalyticsManager?,
) : AwaitDelegate, SavedStateHandleContainer {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<AwaitOutputData> = _outputDataFlow

    override val outputData: AwaitOutputData get() = _outputDataFlow.value

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(AwaitComponentViewType)

    // unused in Await
    override val timerFlow: Flow<TimerData> = flowOf()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var statusPollingJob: Job? = null

    private var action: AwaitAction? by SavedStateHandleProperty(ACTION_KEY)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        restoreState()
    }

    private fun restoreState() {
        adyenLog(AdyenLogLevel.DEBUG) { "Restoring state" }
        action?.let { initState(it) }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            permissionFlow = null,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.repeatOnResume { refreshStatus() }
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        if (action !is AwaitAction) {
            emitError(ComponentException("Unsupported action"))
            return
        }

        this.action = action
        paymentDataRepository.paymentData = action.paymentData

        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager?.trackEvent(event)

        launchAction(action, activity)
        initState(action)
    }

    private fun launchAction(action: AwaitAction, activity: Activity) {
        if (shouldLaunchRedirect(action)) {
            makeRedirect(action, activity)
        }
    }

    private fun shouldLaunchRedirect(action: AwaitAction) = !action.url.isNullOrEmpty()

    private fun makeRedirect(action: AwaitAction, activity: Activity) {
        val url = action.url
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
            redirectHandler.launchUriRedirect(activity, url)
            val paymentData = paymentDataRepository.paymentData
                ?: throw CheckoutException("Payment data should not be null")
            startStatusPolling(paymentData, action)
        } catch (exception: CheckoutException) {
            emitError(exception)
        }
    }

    private fun initState(action: AwaitAction) {
        val paymentData = action.paymentData
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            emitError(ComponentException("Payment data is null"))
            return
        }
        createOutputData(null, action)

        // Redirect flow starts polling after it launched a redirect
        if (!shouldLaunchRedirect(action)) {
            startStatusPolling(paymentData, action)
        }
    }

    private fun startStatusPolling(paymentData: String, action: Action) {
        statusPollingJob?.cancel()
        statusPollingJob = statusRepository.poll(paymentData, DEFAULT_MAX_POLLING_DURATION)
            .onEach { onStatus(it, action) }
            .launchIn(coroutineScope)
    }

    private fun onStatus(result: Result<StatusResponse>, action: Action) {
        result.fold(
            onSuccess = { response ->
                adyenLog(AdyenLogLevel.VERBOSE) { "Status changed - ${response.resultCode}" }
                createOutputData(response, action)
                if (StatusResponseUtils.isFinalResult(response)) {
                    onPollingSuccessful(response)
                }
            },
            onFailure = {
                adyenLog(AdyenLogLevel.ERROR, it) { "Error while polling status" }
                emitError(ComponentException("Error while polling status", it))
            },
        )
    }

    private fun createOutputData(statusResponse: StatusResponse?, action: Action) {
        val isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)
        val outputData = AwaitOutputData(isValid, action.paymentMethodType)
        _outputDataFlow.tryEmit(outputData)
    }

    private fun createOutputData() = AwaitOutputData(
        isValid = false,
        paymentMethodType = null,
    )

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        // Not authorized status should still call /details so that merchant can get more info
        val payload = statusResponse.payload
        if (StatusResponseUtils.isFinalResult(statusResponse) && !payload.isNullOrEmpty()) {
            val details = createDetails(payload)
            emitDetails(details)
        } else {
            emitError(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
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
            emitError(ComponentException("Failed to create details.", e))
        }
        return jsonObject
    }

    private fun emitError(e: CheckoutException) {
        exceptionChannel.trySend(e)
        clearState()
    }

    private fun emitDetails(details: JSONObject) {
        detailsChannel.trySend(createActionComponentData(details))
        clearState()
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        redirectHandler.setOnRedirectListener(listener)
    }

    private fun clearState() {
        action = null
    }

    override fun refreshStatus() {
        val paymentData = paymentDataRepository.paymentData ?: return
        statusRepository.refreshStatus(paymentData)
    }

    override fun onCleared() {
        removeObserver()
        statusPollingJob?.cancel()
        statusPollingJob = null
        _coroutineScope = null
    }

    companion object {
        private val DEFAULT_MAX_POLLING_DURATION = TimeUnit.MINUTES.toMillis(15)

        @VisibleForTesting
        internal const val ACTION_KEY = "ACTION_KEY"

        @VisibleForTesting
        internal const val PAYLOAD_DETAILS_KEY = "payload"
    }
}
