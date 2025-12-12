/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/7/2022.
 */

package com.adyen.checkout.sepa.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.SepaPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.sepa.SepaComponentState
import com.adyen.checkout.sepa.internal.ui.model.SepaInputData
import com.adyen.checkout.sepa.internal.ui.model.SepaOutputData
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultSepaDelegate(
    private val observerRepository: PaymentObserverRepository,
    override val componentParams: ButtonComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<SepaComponentState>,
    private val sdkDataProvider: SdkDataProvider,
) : SepaDelegate {

    private val inputData: SepaInputData = SepaInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<SepaOutputData> = _outputDataFlow

    override val outputData: SepaOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<SepaComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(SepaComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<SepaComponentState> = getTrackedSubmitFlow()
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<SepaComponentState>) -> Unit
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

    override fun updateInputData(update: SepaInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onInputDataChanged" }

        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = SepaOutputData(inputData.name, inputData.iban)

    @VisibleForTesting
    internal fun updateComponentState(outputData: SepaOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: SepaOutputData = this.outputData
    ): SepaComponentState {
        val paymentMethod = SepaPaymentMethod(
            type = SepaPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            sdkData = sdkDataProvider.createEncodedSdkData(),
            ownerName = outputData.ownerNameField.value,
            iban = outputData.ibanNumberField.value,
        )
        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )
        return SepaComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state = state)
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
