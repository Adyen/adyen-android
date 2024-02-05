/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcore.internal.ui

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.IssuerListPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingInputData
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingModel
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingOutputData
import com.adyen.checkout.onlinebankingcore.internal.util.getLegacyIssuers
import com.adyen.checkout.onlinebankingcore.internal.util.mapToModel
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.util.PdfOpener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultOnlineBankingDelegate<
    IssuerListPaymentMethodT : IssuerListPaymentMethod,
    ComponentStateT : PaymentComponentState<IssuerListPaymentMethodT>
    >(
    private val observerRepository: PaymentObserverRepository,
    private val pdfOpener: PdfOpener,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    override val componentParams: ButtonComponentParams,
    private val analyticsRepository: AnalyticsRepository,
    private val termsAndConditionsUrl: String,
    private val submitHandler: SubmitHandler<ComponentStateT>,
    private val paymentMethodFactory: () -> IssuerListPaymentMethodT,
    private val componentStateFactory: (
        data: PaymentComponentData<IssuerListPaymentMethodT>,
        isInputValid: Boolean,
        isReady: Boolean
    ) -> ComponentStateT,
) : OnlineBankingDelegate<IssuerListPaymentMethodT, ComponentStateT> {

    private val inputData: OnlineBankingInputData = OnlineBankingInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<OnlineBankingOutputData> get() = _outputDataFlow

    override val outputData: OnlineBankingOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<ComponentStateT> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(OnlineBankingComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<ComponentStateT> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    init {
        val outputData = OnlineBankingOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        setupAnalytics(coroutineScope)
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "setupAnalytics" }
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<ComponentStateT>) -> Unit
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

    override fun getIssuers(): List<OnlineBankingModel> =
        paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: OnlineBankingInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = OnlineBankingOutputData(inputData.selectedIssuer)

    @VisibleForTesting
    internal fun updateComponentState(outputData: OnlineBankingOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: OnlineBankingOutputData = this.outputData
    ): ComponentStateT {
        val issuerListPaymentMethod = paymentMethodFactory().apply {
            type = getPaymentMethodType()
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId()
            issuer = outputData.selectedIssuer?.id
        }

        val paymentComponentData = PaymentComponentData(
            paymentMethod = issuerListPaymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return componentStateFactory(paymentComponentData, outputData.isValid, true)
    }

    override fun openTermsAndConditions(context: Context) {
        try {
            pdfOpener.open(context, termsAndConditionsUrl)
        } catch (e: IllegalStateException) {
            exceptionChannel.trySend(CheckoutException(e.message ?: "", e.cause))
        }
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
    }
}
