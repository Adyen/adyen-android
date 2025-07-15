/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.googlepay.GooglePayButtonParameters
import com.adyen.checkout.googlepay.GooglePayCancellationException
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayUnavailableException
import com.adyen.checkout.googlepay.internal.data.model.GooglePayPaymentMethodModel
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayOutputData
import com.adyen.checkout.googlepay.internal.util.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.util.GooglePayUtils
import com.adyen.checkout.googlepay.internal.util.awaitTask
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultGooglePayDelegate(
    private val submitHandler: SubmitHandler<GooglePayComponentState>,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: GooglePayComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val paymentsClient: PaymentsClient,
    private val googlePayAvailabilityCheck: GooglePayAvailabilityCheck,
) : GooglePayDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<GooglePayOutputData> = _outputDataFlow
    private val outputData: GooglePayOutputData get() = _outputDataFlow.value

    private val _viewFlow = MutableStateFlow<ComponentViewType>(GooglePayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<GooglePayComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val submitFlow: Flow<GooglePayComponentState> = submitHandler.submitFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val payEventChannel: Channel<Task<PaymentData>> = bufferedChannel()
    override val payEventFlow: Flow<Task<PaymentData>> = payEventChannel.receiveAsFlow()

    private var isAvailable = false

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)
        checkAvailability()
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    private fun checkAvailability() {
        googlePayAvailabilityCheck.isAvailable(
            paymentMethod,
            componentParams,
        ) { isAvailable, _ ->
            this.isAvailable = isAvailable
            updateOutputData(isButtonVisible = isAvailable)

            if (!isAvailable) {
                exceptionChannel.trySend(GooglePayUnavailableException())
            }
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<GooglePayComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = exceptionFlow,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun updateOutputData(
        isButtonVisible: Boolean = this.outputData.isButtonVisible,
        isLoading: Boolean = this.outputData.isLoading,
        paymentData: PaymentData? = this.outputData.paymentData,
    ) {
        val newOutputData = createOutputData(isButtonVisible, isLoading, paymentData)
        _outputDataFlow.tryEmit(newOutputData)
        updateComponentState(newOutputData)
    }

    private fun createOutputData(
        isButtonVisible: Boolean = componentParams.isSubmitButtonVisible,
        isLoading: Boolean = false,
        paymentData: PaymentData? = null,
    ): GooglePayOutputData {
        return GooglePayOutputData(
            isButtonVisible = isButtonVisible,
            isLoading = isLoading,
            paymentData = paymentData,
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: GooglePayOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: GooglePayOutputData = this.outputData
    ): GooglePayComponentState {
        val paymentData = outputData.paymentData
        val isValid = paymentData?.let {
            GooglePayUtils.findToken(it).isNotEmpty()
        } ?: false

        val paymentMethod = GooglePayUtils.createGooglePayPaymentMethod(
            paymentData = paymentData,
            paymentMethodType = paymentMethod.type,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
        )
        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        val isReady = isAvailable

        return GooglePayComponentState(
            data = paymentComponentData,
            isInputValid = isValid,
            isReady = isReady,
            paymentData = paymentData,
        )
    }

    @Deprecated("Deprecated in favor of onSubmit()", replaceWith = ReplaceWith("onSubmit()"))
    override fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        adyenLog(AdyenLogLevel.DEBUG) { "startGooglePayScreen" }
        val paymentsClient = Wallet.getPaymentsClient(activity, GooglePayUtils.createWalletOptions(componentParams))
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(componentParams)
        @Suppress("DEPRECATION")
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), activity, requestCode)
    }

    override fun onSubmit() {
        adyenLog(AdyenLogLevel.DEBUG) { "onSubmit" }

        updateOutputData(isButtonVisible = false, isLoading = true)

        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(componentParams)
        val paymentDataTask = paymentsClient.loadPaymentData(paymentDataRequest)
        coroutineScope.launch {
            payEventChannel.send(paymentDataTask.awaitTask())
        }
    }

    override fun handlePaymentResult(paymentDataTaskResult: ApiTaskResult<PaymentData>) {
        when (val statusCode = paymentDataTaskResult.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                adyenLog(AdyenLogLevel.INFO) { "GooglePay payment result successful" }
                initiatePayment(paymentDataTaskResult.result)
            }

            CommonStatusCodes.CANCELED -> {
                adyenLog(AdyenLogLevel.INFO) { "GooglePay payment canceled" }
                exceptionChannel.trySend(GooglePayCancellationException("GooglePay payment canceled"))
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val statusMessage: String = paymentDataTaskResult.status.statusMessage?.let { ": $it" }.orEmpty()
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an error$statusMessage" }
                trackThirdPartyErrorEvent("Result is error")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an error$statusMessage"))
            }

            CommonStatusCodes.INTERNAL_ERROR -> {
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an internal error" }
                trackThirdPartyErrorEvent("Result is internal error")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an internal error"))
            }

            else -> {
                adyenLog(AdyenLogLevel.ERROR) { "GooglePay encountered an unexpected error, statusCode: $statusCode" }
                trackThirdPartyErrorEvent("Unexpected error")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an unexpected error"))
            }
        }
    }

    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        adyenLog(AdyenLogLevel.DEBUG) { "handleActivityResult" }
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data == null) {
                    trackThirdPartyErrorEvent("Activity result is ok, but data is missing")
                    exceptionChannel.trySend(ComponentException("Result data is null"))
                    return
                }
                initiatePayment(PaymentData.getFromIntent(data))
            }

            Activity.RESULT_CANCELED -> {
                exceptionChannel.trySend(GooglePayCancellationException("Payment canceled."))
            }

            AutoResolveHelper.RESULT_ERROR -> {
                trackThirdPartyErrorEvent("Activity result is error")

                val status = AutoResolveHelper.getStatusFromIntent(data)
                val statusMessage: String = status?.let { ": ${it.statusMessage}" }.orEmpty()
                exceptionChannel.trySend(ComponentException("GooglePay returned an error$statusMessage"))
            }
        }
    }

    private fun initiatePayment(paymentData: PaymentData?) {
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            trackThirdPartyErrorEvent("Result is success, but data is missing")
            exceptionChannel.trySend(ComponentException("GooglePay encountered an unexpected error"))
            return
        }
        adyenLog(AdyenLogLevel.INFO) { "GooglePay payment result successful" }

        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        updateOutputData(paymentData = paymentData)
        submitHandler.onSubmit(_componentStateFlow.value)
    }

    private fun trackThirdPartyErrorEvent(message: String) {
        val event = GenericEvents.error(
            component = getPaymentMethodType(),
            event = ErrorEvent.THIRD_PARTY,
            message = message,
        )
        analyticsManager.trackEvent(event)
    }

    override fun getGooglePayButtonParameters(): GooglePayButtonParameters {
        val allowedPaymentMethodsList = GooglePayUtils.getAllowedPaymentMethods(componentParams)
        val allowedPaymentMethods = ModelUtils.serializeOptList(
            allowedPaymentMethodsList,
            GooglePayPaymentMethodModel.SERIALIZER,
        )?.toString().orEmpty()
        return GooglePayButtonParameters(allowedPaymentMethods)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    internal fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
