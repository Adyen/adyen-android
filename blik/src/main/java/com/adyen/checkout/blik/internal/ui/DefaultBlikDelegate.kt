/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.blik.internal.ui.model.BlikInputData
import com.adyen.checkout.blik.internal.ui.model.BlikOutputData
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.ButtonComponentParams
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.PaymentComponentUIEvent
import com.adyen.checkout.components.ui.PaymentComponentUIState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.view.ButtonComponentViewType
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultBlikDelegate(
    private val observerRepository: PaymentObserverRepository,
    override val componentParams: ButtonComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    private val analyticsRepository: AnalyticsRepository,
    private val submitHandler: SubmitHandler<PaymentComponentState<BlikPaymentMethod>>,
) : BlikDelegate {

    private val inputData: BlikInputData = BlikInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BlikOutputData> = _outputDataFlow

    override val outputData: BlikOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(BlikComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<PaymentComponentState<BlikPaymentMethod>> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    init {
        updateComponentState(outputData)
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        sendAnalyticsEvent(coroutineScope)
    }

    private fun sendAnalyticsEvent(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "sendAnalyticsEvent")
        coroutineScope.launch {
            analyticsRepository.sendAnalyticsEvent()
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PaymentComponentState<BlikPaymentMethod>>) -> Unit
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

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: BlikInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        val outputData = createOutputData()
        outputDataChanged(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = BlikOutputData(inputData.blikCode)

    private fun outputDataChanged(outputData: BlikOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: BlikOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: BlikOutputData = this.outputData
    ): PaymentComponentState<BlikPaymentMethod> {
        val paymentMethod = BlikPaymentMethod(
            type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
            blikCode = outputData.blikCodeField.value
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
        )

        return PaymentComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true
        )
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
