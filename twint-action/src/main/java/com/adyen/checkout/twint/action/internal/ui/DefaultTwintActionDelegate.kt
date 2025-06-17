/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.action.internal.ui

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.StatusRepository
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
internal class DefaultTwintActionDelegate(
    private val observerRepository: ActionObserverRepository,
    override val savedStateHandle: SavedStateHandle,
    override val componentParams: GenericComponentParams,
    private val paymentDataRepository: PaymentDataRepository,
    private val statusRepository: StatusRepository,
    private val analyticsManager: AnalyticsManager?,
) : TwintActionDelegate, SavedStateHandleContainer {

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(TwintActionComponentViewType)

    // Not used for Twint action
    override val timerFlow: Flow<TimerData> = flow {}

    private val payEventChannel: Channel<TwintFlowType> = bufferedChannel()
    override val payEventFlow: Flow<TwintFlowType> = payEventChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var statusPollingJob: Job? = null

    private var action: SdkAction<TwintSdkData>? by SavedStateHandleProperty(ACTION_KEY)
    private var isPolling: Boolean? by SavedStateHandleProperty(IS_POLLING_KEY)

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
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        if (action !is SdkAction<*>) {
            emitError(ComponentException("Unsupported action"))
            return
        }

        val sdkData = action.sdkData
        if (action.sdkData == null || sdkData !is TwintSdkData) {
            emitError(ComponentException("SDK Data is null or of wrong type"))
            return
        }

        @Suppress("UNCHECKED_CAST")
        this.action = action as SdkAction<TwintSdkData>

        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager?.trackEvent(event)

        initState(action)
        launchAction(sdkData)
    }

    private fun initState(action: SdkAction<TwintSdkData>) {
        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            emitError(ComponentException("Payment data is null"))
            return
        }

        if (isPolling == true) {
            startStatusPolling()
        }
    }

    private fun launchAction(sdkData: TwintSdkData) {
        val flowType = if (sdkData.isStored) {
            TwintFlowType.Recurring(sdkData.token)
        } else {
            TwintFlowType.OneTime(sdkData.token)
        }
        payEventChannel.trySend(flowType)
    }

    override fun handleTwintResult(result: TwintPayResult) {
        when (result) {
            TwintPayResult.TW_B_SUCCESS -> {
                startStatusPolling()
            }

            TwintPayResult.TW_B_ERROR -> {
                trackThirdPartyErrorEvent("Twint result is error")
                onError(ComponentException("Twint encountered an error."))
            }

            TwintPayResult.TW_B_APP_NOT_INSTALLED -> {
                trackThirdPartyErrorEvent("Twint app not installed")
                onError(ComponentException("Twint app not installed."))
            }
        }
    }

    private fun startStatusPolling() {
        isPolling = true
        statusPollingJob?.cancel()

        val paymentData = paymentDataRepository.paymentData
        if (paymentData == null) {
            emitError(ComponentException("PaymentData should not be null."))
            return
        }

        statusPollingJob = statusRepository.poll(paymentData, DEFAULT_MAX_POLLING_DURATION)
            .onEach { onStatus(it) }
            .launchIn(coroutineScope)
    }

    private fun onStatus(result: Result<StatusResponse>) {
        result.fold(
            onSuccess = { response ->
                adyenLog(AdyenLogLevel.VERBOSE) { "Status changed - ${response.resultCode}" }
                onPollingSuccessful(response)
            },
            onFailure = {
                adyenLog(AdyenLogLevel.ERROR, it) { "Error while polling status" }
                emitError(ComponentException("Error while polling status.", it))
            },
        )
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        val payload = statusResponse.payload
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse)) {
            if (!payload.isNullOrEmpty()) {
                emitDetails(payload)
            } else {
                emitError(ComponentException("Payload is missing from StatusResponse."))
            }
        }
    }

    private fun createActionComponentData(payload: String): ActionComponentData {
        return ActionComponentData(
            details = JSONObject().put(PAYLOAD_DETAILS_KEY, payload),
            // We don't share paymentData on purpose, so merchant will not use it to build their own polling.
            paymentData = null,
        )
    }

    private fun trackThirdPartyErrorEvent(message: String) {
        val event = GenericEvents.error(
            component = action?.paymentMethodType.orEmpty(),
            event = ErrorEvent.THIRD_PARTY,
            message = message
        )
        analyticsManager?.trackEvent(event)
    }

    override fun onError(e: CheckoutException) {
        emitError(e)
    }

    override fun refreshStatus() {
        if (statusPollingJob == null) return
        val paymentData = paymentDataRepository.paymentData ?: return
        statusRepository.refreshStatus(paymentData)
    }

    private fun emitError(e: CheckoutException) {
        exceptionChannel.trySend(e)
        clearState()
    }

    private fun emitDetails(payload: String) {
        detailsChannel.trySend(createActionComponentData(payload))
        clearState()
    }

    private fun clearState() {
        action = null
        isPolling = null
    }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val DEFAULT_MAX_POLLING_DURATION = TimeUnit.MINUTES.toMillis(15)

        @VisibleForTesting
        internal const val ACTION_KEY = "ACTION_KEY"

        @VisibleForTesting
        internal const val IS_POLLING_KEY = "IS_POLLING_KEY"

        @VisibleForTesting
        internal const val PAYLOAD_DETAILS_KEY = "payload"
    }
}
