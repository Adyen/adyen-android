/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.await.internal.ui.view.AwaitComponent
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.AwaitAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.ComponentError
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.data.api.StatusRepository
import com.adyen.checkout.core.components.internal.data.api.helper.isFinalResult
import com.adyen.checkout.core.components.internal.data.model.StatusResponse
import com.adyen.checkout.core.components.internal.ui.StatusPollingComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.checkout.core.redirect.internal.ui.RedirectViewEvent
import com.adyen.checkout.core.redirect.internal.ui.redirectEvent
import com.adyen.checkout.ui.internal.ComponentScaffold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// TODO - Write tests
@Suppress("TooManyFunctions", "LongParameterList")
internal class AwaitComponent(
    private val action: AwaitAction,
    private val coroutineScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager,
    private val redirectHandler: RedirectHandler,
    private val statusRepository: StatusRepository,
    private val paymentDataRepository: PaymentDataRepository,
    override val componentParams: ComponentParams,
) : ActionComponent, StatusPollingComponent {

    private var statusPollingJob: Job? = null

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    private val redirectEventChannel = bufferedChannel<RedirectViewEvent>()
    private val redirectEventFlow: Flow<RedirectViewEvent> = redirectEventChannel.receiveAsFlow()

    override fun handleAction() {
        paymentDataRepository.paymentData = action.paymentData

        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager.trackEvent(event)

        launchActionIfRedirect(action)
        initState(action)
    }

    private fun launchActionIfRedirect(action: AwaitAction) {
        val url = action.url
        if (!url.isNullOrEmpty()) {
            makeRedirect(url)
        }
    }

    @Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
    private fun makeRedirect(redirectUrl: String) {
        redirectEventChannel.trySend(RedirectViewEvent.Redirect(redirectUrl))
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $redirectUrl" }
            val paymentData = paymentDataRepository.paymentData
                // TODO - Error Propagation
                // ?: throw CheckoutException("Payment data should not be null")
                ?: throw RuntimeException("Payment data should not be null")
            startStatusPolling(paymentData)
            // TODO - Error Propagation
        } catch (exception: RuntimeException) {
            emitError(exception)
        }
    }

    private fun initState(action: AwaitAction) {
        val paymentData = action.paymentData
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            // TODO - Throw exception?
            return
        }

        // Redirect flow starts polling after it launched a redirect
        if (action.url.isNullOrEmpty()) {
            startStatusPolling(paymentData)
        }
    }

    // TODO - Move status polling into a separate class to not duplicate it in other component
    private fun startStatusPolling(paymentData: String) {
        statusPollingJob?.cancel()
        statusPollingJob = statusRepository.poll(paymentData, DEFAULT_MAX_POLLING_DURATION)
            .onEach { onStatus(it) }
            .launchIn(coroutineScope)
    }

    private fun onStatus(result: Result<StatusResponse>) {
        result.fold(
            onSuccess = { response ->
                adyenLog(AdyenLogLevel.VERBOSE) { "Status changed - ${response.resultCode}" }
                if (response.isFinalResult()) {
                    onPollingSuccessful(response)
                }
            },
            onFailure = {
                adyenLog(AdyenLogLevel.ERROR, it) { "Error while polling status" }
                // TODO - Throw exception?
            },
        )
    }

    private fun onPollingSuccessful(statusResponse: StatusResponse) {
        // Not authorized status should still call /details so that merchant can get more info
        val payload = statusResponse.payload
        if (statusResponse.isFinalResult() && !payload.isNullOrEmpty()) {
            emitDetails(payload)
        } else {
            // TODO - Error propagation
//            emitError(ComponentException("Payment was not completed. - " + statusResponse.resultCode))
            emitError(RuntimeException("Payment was not completed. - " + statusResponse.resultCode))
        }
    }

    private fun emitDetails(payload: String) {
        try {
            val jsonObject = JSONObject().apply {
                put(PAYLOAD_DETAILS_KEY, payload)
            }
            emitDetails(jsonObject)
        } catch (e: JSONException) {
            // TODO - Error propagation
//            emitError(ComponentException("Failed to create details.", e))
            emitError(RuntimeException("Failed to create details.", e))
        }
    }

    private fun emitDetails(details: JSONObject) {
        eventChannel.trySend(
            ActionComponentEvent.ActionDetails(createActionComponentData(details)),
        )
    }

    // TODO - Error propagation
    private fun emitError(e: RuntimeException) {
        eventChannel.trySend(
            ActionComponentEvent.Error(ComponentError(e)),
        )
        statusPollingJob?.cancel()
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        redirectEvent(
            redirectHandler = redirectHandler,
            viewEventFlow = redirectEventFlow,
            onError = ::emitError,
        )

        ComponentScaffold(
            modifier = modifier,
        ) {
            AwaitComponent()
        }
    }

    companion object {
        private val DEFAULT_MAX_POLLING_DURATION = TimeUnit.MINUTES.toMillis(15)

        @VisibleForTesting
        internal const val PAYLOAD_DETAILS_KEY = "payload"
    }
}
