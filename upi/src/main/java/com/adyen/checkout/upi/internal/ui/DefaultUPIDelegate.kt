/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.UPIPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.internal.ui.model.UPIInputData
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal class DefaultUPIDelegate(
    private val submitHandler: SubmitHandler<UPIComponentState>,
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
) : UPIDelegate {

    private val inputData = UPIInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<UPIOutputData> = _outputDataFlow

    override val outputData: UPIOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<UPIComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(UPIComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<UPIComponentState> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<UPIComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun updateInputData(update: UPIInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onInputDataChanged" }
        val outputData = createOutputData()
        outputDataChanged(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = with(inputData) {
        UPIOutputData(
            mode = mode,
            virtualPaymentAddress = virtualPaymentAddress,
        )
    }

    private fun outputDataChanged(outputData: UPIOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: UPIOutputData) {
        val componentState = createComponentState(outputData)
        componentStateChanged(componentState)
    }

    private fun createComponentState(
        outputData: UPIOutputData = this.outputData
    ): UPIComponentState {
        val paymentMethod = UPIPaymentMethod(
            type = if (outputData.mode == UPIMode.VPA) PaymentMethodTypes.UPI_COLLECT else PaymentMethodTypes.UPI_QR,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            virtualPaymentAddress = if (outputData.mode == UPIMode.VPA) {
                outputData.virtualPaymentAddressFieldState.value
            } else {
                null
            },
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return UPIComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    private fun componentStateChanged(componentState: UPIComponentState) {
        _componentStateFlow.tryEmit(componentState)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onSubmit() {
        submitHandler.onSubmit(_componentStateFlow.value)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
