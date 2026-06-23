/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayIntent
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.adyen.checkout.googlepay.internal.ui.state.toPaymentComponentState
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class GooglePayComponent(
    private val analyticsManager: AnalyticsManager,
    private val componentParams: GooglePayComponentParams,
    private val sdkDataProvider: SdkDataProvider,
    private val paymentMethodType: String,
    private val componentStateValidator: GooglePayComponentStateValidator,
    componentStateFactory: GooglePayComponentStateFactory,
    componentStateReducer: GooglePayComponentStateReducer,
    viewStateProducer: GooglePayViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val viewEventChannel = bufferedChannel<GooglePayViewEvent>()
    private val viewEventFlow: Flow<GooglePayViewEvent> = viewEventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    internal val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        analyticsManager.initialize(this, coroutineScope)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        googlePayEvent(
            componentParams = componentParams,
            viewEventFlow = viewEventFlow,
            onResult = ::onPaymentResult,
        )

        // TODO - Render the Google Pay button.
        Box(modifier = modifier)
    }

    override fun submit() {
        setLoading(true)
        viewEventChannel.trySend(GooglePayViewEvent.Pay)
    }

    @VisibleForTesting
    internal fun onPaymentResult(result: ApiTaskResult<PaymentData>) {
        when (val statusCode = result.status.statusCode) {
            CommonStatusCodes.SUCCESS -> handleSuccessResult(result.result)

            CommonStatusCodes.CANCELED -> {
                adyenLog(AdyenLogLevel.INFO) { "GooglePay payment canceled" }
                setLoading(false)
            }

            else -> {
                val statusMessage = result.status.statusMessage?.let { ": $it" }.orEmpty()
                adyenLog(AdyenLogLevel.ERROR) {
                    "GooglePay encountered an error$statusMessage, statusCode: $statusCode"
                }
                trackThirdPartyErrorEvent("Result is error$statusMessage")
                emitError(GenericError("GooglePay encountered an error$statusMessage"))
            }
        }
    }

    private fun handleSuccessResult(paymentData: PaymentData?) {
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "GooglePay payment data is null" }
            trackThirdPartyErrorEvent("Result is success, but data is missing")
            emitError(GenericError("GooglePay encountered an unexpected error"))
            return
        }
        adyenLog(AdyenLogLevel.INFO) { "GooglePay payment result successful" }

        analyticsManager.trackEvent(GenericEvents.submit(paymentMethodType))

        componentState.handleIntent(GooglePayIntent.UpdatePaymentData(paymentData))

        val paymentComponentState = componentState.value
            .copy(paymentData = paymentData)
            .toPaymentComponentState(
                paymentMethodType = paymentMethodType,
                sdkDataProvider = sdkDataProvider,
            )
        eventChannel.trySend(PaymentComponentEvent.Submit(paymentComponentState))
    }

    private fun emitError(error: InternalCheckoutError) {
        setLoading(false)
        eventChannel.trySend(PaymentComponentEvent.Error(error))
    }

    private fun trackThirdPartyErrorEvent(message: String) {
        val event = GenericEvents.error(
            component = paymentMethodType,
            event = ErrorEvent.THIRD_PARTY,
            message = message,
        )
        analyticsManager.trackEvent(event)
    }

    override fun requiresUserInteraction(): Boolean = true

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(GooglePayIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
