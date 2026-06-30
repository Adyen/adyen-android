/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import androidx.annotation.VisibleForTesting
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
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.adyen.checkout.googlepay.internal.helper.awaitTask
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayIntent
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.adyen.checkout.googlepay.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.googlepay.internal.ui.view.GooglePayContent
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
internal class GooglePayComponent(
    @Suppress("unused") private val analyticsManager: AnalyticsManager,
    private val componentParams: GooglePayComponentParams,
    private val sdkDataProvider: SdkDataProvider,
    private val googlePayAvailabilityCheck: GooglePayAvailabilityCheck,
    private val paymentMethodType: String,
    private val componentStateValidator: GooglePayComponentStateValidator,
    componentStateFactory: GooglePayComponentStateFactory,
    componentStateReducer: GooglePayComponentStateReducer,
    viewStateProducer: GooglePayViewStateProducer,
    private val coroutineScope: CoroutineScope,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    // Launching the Google Pay sheet requires an ActivityResultLauncher that can only be created from a
    // Composable (see GooglePayContent). As submit() is triggered outside of composition, we bridge the
    // request to the Composable layer by sending a view event through this channel.
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
        checkAvailability()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        GooglePayContent(
            viewStateFlow = viewState,
            viewEventFlow = viewEventFlow,
            onResult = ::onPaymentResult,
            loadPaymentData = ::loadPaymentData,
            onSubmit = ::submit,
            modifier = modifier,
        )
    }

    // TODO - Expose a merchant-facing Google Pay availability pre-check API (COSDK-1310).
    private fun checkAvailability() {
        coroutineScope.launch {
            val isAvailable = googlePayAvailabilityCheck.isAvailable()
            componentState.handleIntent(GooglePayIntent.UpdateAvailability(isAvailable))
        }
    }

    private suspend fun loadPaymentData(context: Context): Task<PaymentData> {
        val paymentsClient = Wallet.getPaymentsClient(
            context,
            GooglePayUtils.createWalletOptions(componentParams),
        )
        return paymentsClient
            .loadPaymentData(GooglePayUtils.createPaymentDataRequest(componentParams))
            .awaitTask()
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

    override fun onCleared() = Unit

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
