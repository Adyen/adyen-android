/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayMainNavigationKey
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.adyen.checkout.googlepay.internal.helper.awaitTask
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayIntent
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayPaymentComponentState
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewState
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.adyen.checkout.googlepay.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.googlepay.internal.ui.view.GooglePayScreen
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
internal class GooglePayComponent(
    private val componentParams: GooglePayComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
    private val paymentsClient: PaymentsClient,
    private val coroutineScope: CoroutineScope,
    private val paymentMethodType: String?,
    private val componentStateValidator: GooglePayComponentStateValidator,
    componentStateFactory: GooglePayComponentStateFactory,
    componentStateReducer: GooglePayComponentStateReducer,
    viewStateProducer: GooglePayViewStateProducer,
) : PaymentComponent<GooglePayPaymentComponentState> {

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        GooglePayNavKey to CheckoutNavEntry(GooglePayNavKey, GooglePayMainNavigationKey) { backStack ->
            MainScreen(backStack)
        },
    )

    override val navigationStartingPoint: NavKey = GooglePayNavKey

    private val eventChannel = bufferedChannel<PaymentComponentEvent<GooglePayPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<GooglePayPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val viewEventChannel = bufferedChannel<GooglePayViewEvent>()
    val viewEventFlow: Flow<GooglePayViewEvent> = viewEventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState: StateFlow<GooglePayViewState> = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        initializeAnalytics()
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize(this, coroutineScope)
    }

    override fun submit() {
        adyenLog(AdyenLogLevel.DEBUG) { "submit" }
        onIntent(GooglePayIntent.UpdateLoading(true))

        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(componentParams)
        val paymentDataTask = paymentsClient.loadPaymentData(paymentDataRequest)
        coroutineScope.launch {
            viewEventChannel.send(GooglePayViewEvent.LaunchGooglePay(paymentDataTask.awaitTask()))
        }
    }

    fun handlePaymentResult(paymentDataTaskResult: ApiTaskResult<PaymentData>) {
        when (val statusCode = paymentDataTaskResult.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                adyenLog(AdyenLogLevel.INFO) { "GooglePay payment result successful" }
                initiatePayment(paymentDataTaskResult.result)
            }

            CommonStatusCodes.CANCELED -> {
                adyenLog(AdyenLogLevel.INFO) { "GooglePay payment canceled" }
                onIntent(GooglePayIntent.UpdateLoading(false))
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val statusMessage: String = paymentDataTaskResult.status.statusMessage?.let { ": $it" }.orEmpty()
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an error$statusMessage" }
                onIntent(GooglePayIntent.UpdateLoading(false))
            }

            CommonStatusCodes.INTERNAL_ERROR -> {
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an internal error" }
                onIntent(GooglePayIntent.UpdateLoading(false))
            }

            else -> {
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an unexpected error, statusCode: $statusCode" }
                onIntent(GooglePayIntent.UpdateLoading(false))
            }
        }
    }

    private fun initiatePayment(paymentData: PaymentData?) {
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            onIntent(GooglePayIntent.UpdateLoading(false))
            return
        }

        onIntent(GooglePayIntent.UpdatePaymentData(paymentData))

        val paymentComponentState = componentState.value.toPaymentComponentState(
            amount = componentParams.amount,
            paymentMethodType = paymentMethodType,
            sdkDataProvider = sdkDataProvider,
        )

        eventChannel.trySend(PaymentComponentEvent.Submit(paymentComponentState))
    }

    override fun setLoading(isLoading: Boolean) {
        onIntent(GooglePayIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    private fun onIntent(intent: GooglePayIntent) {
        componentState.handleIntent(intent)
    }

    @Composable
    @Suppress("UNUSED_PARAMETER")
    private fun MainScreen(backStack: NavBackStack<NavKey>) {
        val viewState by viewState.collectAsStateWithLifecycle()

        googlePayEvent(
            viewEventFlow = viewEventFlow,
            onPaymentResult = ::handlePaymentResult,
        )

        GooglePayScreen(
            viewState = viewState,
            onSubmitClick = ::submit,
        )
    }

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
