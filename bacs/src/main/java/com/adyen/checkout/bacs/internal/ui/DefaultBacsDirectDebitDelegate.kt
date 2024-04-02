/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2022.
 */

package com.adyen.checkout.bacs.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitInputData
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitOutputData
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.BacsDirectDebitPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal class DefaultBacsDirectDebitDelegate(
    private val observerRepository: PaymentObserverRepository,
    override val componentParams: ButtonComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<BacsDirectDebitComponentState>,
) : BacsDirectDebitDelegate {

    private val inputData: BacsDirectDebitInputData = BacsDirectDebitInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BacsDirectDebitOutputData> = _outputDataFlow

    override val outputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<BacsDirectDebitComponentState> = _componentStateFlow

    override val submitFlow: Flow<BacsDirectDebitComponentState> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(BacsComponentViewType.INPUT)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

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
        callback: (PaymentComponentEvent<BacsDirectDebitComponentState>) -> Unit
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

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun setMode(mode: BacsDirectDebitMode): Boolean {
        val currentMode = inputData.mode
        return when {
            mode == currentMode -> {
                adyenLog(AdyenLogLevel.ERROR) { "Current mode is already $mode" }
                false
            }

            mode == BacsDirectDebitMode.CONFIRMATION && !outputData.isValid -> {
                adyenLog(AdyenLogLevel.ERROR) { "Cannot set confirmation view when input is not valid" }
                false
            }

            else -> {
                adyenLog(AdyenLogLevel.DEBUG) { "Setting mode to $mode" }
                updateInputData { this.mode = mode }
                true
            }
        }
    }

    override fun updateInputData(update: BacsDirectDebitInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        val state = _componentStateFlow.value
        when (inputData.mode) {
            BacsDirectDebitMode.INPUT -> {
                if (outputData.isValid) {
                    setMode(BacsDirectDebitMode.CONFIRMATION)
                } else {
                    // If mode is input and input is not valid this triggers an InvalidUI event
                    submitHandler.onSubmit(state)
                }
            }

            BacsDirectDebitMode.CONFIRMATION -> {
                submitHandler.onSubmit(state)
            }
        }
    }

    override fun handleBackPress(): Boolean {
        val isConfirmationMode = _componentStateFlow.value.mode == BacsDirectDebitMode.CONFIRMATION
        return if (isConfirmationMode) {
            setMode(BacsDirectDebitMode.INPUT)
            true
        } else {
            false
        }
    }

    private fun onInputDataChanged() {
        updateViewType(inputData.mode)

        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun updateViewType(mode: BacsDirectDebitMode) {
        val viewType = when (mode) {
            BacsDirectDebitMode.INPUT -> BacsComponentViewType.INPUT
            BacsDirectDebitMode.CONFIRMATION -> BacsComponentViewType.CONFIRMATION
        }
        if (_viewFlow.value != viewType) {
            adyenLog(AdyenLogLevel.DEBUG) { "Updating view flow to $viewType" }
            _viewFlow.tryEmit(viewType)
        }
    }

    private fun createOutputData() = BacsDirectDebitOutputData(
        holderNameState = BacsDirectDebitValidationUtils.validateHolderName(inputData.holderName),
        bankAccountNumberState = BacsDirectDebitValidationUtils.validateBankAccountNumber(inputData.bankAccountNumber),
        sortCodeState = BacsDirectDebitValidationUtils.validateSortCode(inputData.sortCode),
        shopperEmailState = BacsDirectDebitValidationUtils.validateShopperEmail(inputData.shopperEmail),
        isAmountConsentChecked = inputData.isAmountConsentChecked,
        isAccountConsentChecked = inputData.isAccountConsentChecked,
        mode = inputData.mode,
    )

    @VisibleForTesting
    internal fun updateComponentState(outputData: BacsDirectDebitOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: BacsDirectDebitOutputData = this.outputData
    ): BacsDirectDebitComponentState {
        val bacsDirectDebitPaymentMethod = BacsDirectDebitPaymentMethod(
            type = BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            holderName = outputData.holderNameState.value,
            bankAccountNumber = outputData.bankAccountNumberState.value,
            bankLocationId = outputData.sortCodeState.value,
        )

        val paymentComponentData = PaymentComponentData(
            shopperEmail = outputData.shopperEmailState.value,
            paymentMethod = bacsDirectDebitPaymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return BacsDirectDebitComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
            mode = outputData.mode,
        )
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
