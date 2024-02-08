/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.googlepay.GooglePayButtonParameters
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.internal.data.model.GooglePayPaymentMethodModel
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.util.GooglePayUtils
import com.adyen.checkout.googlepay.internal.util.awaitTask
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultGooglePayDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: GooglePayComponentParams,
    private val analyticsRepository: AnalyticsRepository,
    private val application: Application,
) : GooglePayDelegate {

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<GooglePayComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val submitChannel: Channel<GooglePayComponentState> = bufferedChannel()
    override val submitFlow: Flow<GooglePayComponentState> = submitChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        setupAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "setupAnalytics")
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    private fun onState(state: GooglePayComponentState) {
        if (state.isValid) {
            submitChannel.trySend(state)
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

    @VisibleForTesting
    internal fun updateComponentState(paymentData: PaymentData?) {
        Logger.v(TAG, "updateComponentState")
        val componentState = createComponentState(paymentData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(paymentData: PaymentData? = null): GooglePayComponentState {
        val isValid = paymentData?.let {
            GooglePayUtils.findToken(it).isNotEmpty()
        } ?: false

        val paymentMethod = GooglePayUtils.createGooglePayPaymentMethod(
            paymentData = paymentData,
            paymentMethodType = paymentMethod.type,
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId(),
        )
        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return GooglePayComponentState(
            data = paymentComponentData,
            isInputValid = isValid,
            isReady = true,
            paymentData = paymentData,
        )
    }

    override fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        Logger.d(TAG, "startGooglePayScreen")
        val paymentsClient = Wallet.getPaymentsClient(activity, GooglePayUtils.createWalletOptions(componentParams))
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(componentParams)
        // TODO this forces us to use the deprecated onActivityResult. Look into alternatives when/if Google provides
        //  any later.
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), activity, requestCode)
    }

    override fun startGooglePayScreen(paymentDataLauncher: ActivityResultLauncher<Task<PaymentData>>) {
        Logger.d(TAG, "startGooglePayScreen")
        val paymentsClient = Wallet.getPaymentsClient(application, GooglePayUtils.createWalletOptions(componentParams))
        val paymentDataRequest = GooglePayUtils.createPaymentDataRequest(componentParams)
        val paymentDataTask = paymentsClient.loadPaymentData(paymentDataRequest)
        coroutineScope.launch {
            paymentDataLauncher.launch(paymentDataTask.awaitTask())
        }
    }

    override fun handlePaymentResult(paymentDataTaskResult: ApiTaskResult<PaymentData>?) {
        if (paymentDataTaskResult == null) {
            Logger.e(TAG, "GooglePay ApiTaskResult is null")
            exceptionChannel.trySend(ComponentException("GooglePay encountered an unexpected error"))
            return
        }
        when (val statusCode = paymentDataTaskResult.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                val paymentData = paymentDataTaskResult.result
                if (paymentData == null) {
                    Logger.e(TAG, "Result data is null")
                    exceptionChannel.trySend(ComponentException("GooglePay encountered an unexpected error"))
                    return
                }
                Logger.i(TAG, "GooglePay payment result successful")
                updateComponentState(paymentData)
            }

            CommonStatusCodes.CANCELED -> {
                Logger.i(TAG, "GooglePay payment canceled")
                exceptionChannel.trySend(ComponentException("GooglePay payment canceled"))
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val statusMessage: String = paymentDataTaskResult.status.statusMessage?.let { ": $it" }.orEmpty()
                Logger.e(TAG, "GooglePay encountered an error$statusMessage")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an error$statusMessage"))
            }

            CommonStatusCodes.INTERNAL_ERROR -> {
                Logger.e(TAG, "GooglePay encountered an internal error")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an internal error"))
            }

            else -> {
                Logger.e(TAG, "GooglePay encountered an unexpected error, statusCode: $statusCode")
                exceptionChannel.trySend(ComponentException("GooglePay encountered an unexpected error"))
            }
        }
    }

    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        Logger.d(TAG, "handleActivityResult")
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data == null) {
                    exceptionChannel.trySend(ComponentException("Result data is null"))
                    return
                }
                val paymentData = PaymentData.getFromIntent(data)
                updateComponentState(paymentData)
            }

            Activity.RESULT_CANCELED -> {
                exceptionChannel.trySend(ComponentException("Payment canceled."))
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                val statusMessage: String = status?.let { ": ${it.statusMessage}" }.orEmpty()
                exceptionChannel.trySend(ComponentException("GooglePay returned an error$statusMessage"))
            }

            else -> Unit
        }
    }

    override fun getGooglePayButtonParameters(): GooglePayButtonParameters {
        val allowedPaymentMethodsList = GooglePayUtils.getAllowedPaymentMethods(componentParams)
        val allowedPaymentMethods = ModelUtils.serializeOptList(
            allowedPaymentMethodsList,
            GooglePayPaymentMethodModel.SERIALIZER,
        )?.toString().orEmpty()
        return GooglePayButtonParameters(allowedPaymentMethods)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
