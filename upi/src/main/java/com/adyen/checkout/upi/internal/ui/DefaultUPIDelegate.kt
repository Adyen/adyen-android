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
import com.adyen.checkout.components.core.AppData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.paymentmethod.UPIPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.upi.R
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.internal.ui.model.UPIInputData
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import com.adyen.checkout.upi.internal.ui.model.UPISelectedMode
import com.adyen.checkout.upi.internal.ui.model.mapToSelectedMode
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

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
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

    private fun createOutputData(includeValidationErrors: Boolean = false) = with(inputData) {
        val availableModes = createAvailableModes(this, paymentMethod, includeValidationErrors)
        val intentVirtualPaymentAddressFieldState = validateVirtualPaymentAddress(intentVirtualPaymentAddress)

        UPIOutputData(
            selectedMode = selectedMode ?: availableModes.first().mapToSelectedMode(),
            selectedUPIIntentItem = selectedUPIIntentItem,
            availableModes = availableModes,
            virtualPaymentAddressFieldState = validateVirtualPaymentAddress(vpaVirtualPaymentAddress),
            intentVirtualPaymentAddressFieldState = intentVirtualPaymentAddressFieldState,
        )
    }

    private fun createAvailableModes(
        inputData: UPIInputData,
        paymentMethod: PaymentMethod,
        includeValidation: Boolean
    ) = with(inputData) {
        val appIds = paymentMethod.apps
        if (!appIds.isNullOrEmpty()) {
            val paymentAddressFieldState = if (includeValidation) {
                validateVirtualPaymentAddress(intentVirtualPaymentAddress)
            } else {
                null
            }
            val intentItemList = createIntentItems(
                appIds,
                componentParams.environment,
                selectedUPIIntentItem,
                paymentAddressFieldState,
            )

            listOf(UPIMode.Intent(intentItemList), UPIMode.Qr)
        } else {
            listOf(UPIMode.Vpa, UPIMode.Qr)
        }
    }

    private fun createIntentItems(
        upiApps: List<AppData>,
        environment: Environment,
        selectedUPIIntentItem: UPIIntentItem?,
        paymentAddressFieldState: FieldState<String>?,
    ): List<UPIIntentItem> {
        val paymentApps = upiApps.mapToPaymentApp(
            environment = environment,
            selectedAppId = (selectedUPIIntentItem as? UPIIntentItem.PaymentApp)?.id,
        )

        val genericApp = UPIIntentItem.GenericApp(
            isSelected = selectedUPIIntentItem is UPIIntentItem.GenericApp,
        )

        val manualInputErrorMessageId = paymentAddressFieldState?.let {
            getValidationErrorResourceIdOrNull(paymentAddressFieldState.validation)
        }
        val manualInput = UPIIntentItem.ManualInput(
            errorMessageResource = manualInputErrorMessageId,
            isSelected = selectedUPIIntentItem is UPIIntentItem.ManualInput,
        )

        return mutableListOf<UPIIntentItem>().apply {
            addAll(paymentApps)
            add(genericApp)
            add(manualInput)
        }
    }

    private fun getValidationErrorResourceIdOrNull(validation: Validation?): Int? =
        (validation as? Validation.Invalid)?.reason

    private fun validateVirtualPaymentAddress(virtualPaymentAddress: String): FieldState<String> =
        if (virtualPaymentAddress.isNotBlank()) {
            FieldState(virtualPaymentAddress, Validation.Valid)
        } else {
            FieldState(virtualPaymentAddress, Validation.Invalid(R.string.checkout_upi_vpa_validation))
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
            type = getUPIPaymentMethodType(outputData),
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            appId = getIntentItemAppIdForComponentStateOrNull(outputData),
            virtualPaymentAddress = getVirtualPaymentAddress(outputData),
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

    private fun getUPIPaymentMethodType(outputData: UPIOutputData) = when (outputData.selectedMode) {
        UPISelectedMode.INTENT -> {
            when (outputData.selectedUPIIntentItem) {
                is UPIIntentItem.PaymentApp -> {
                    PaymentMethodTypes.UPI_INTENT
                }

                is UPIIntentItem.GenericApp -> {
                    PaymentMethodTypes.UPI_INTENT
                }

                is UPIIntentItem.ManualInput -> {
                    PaymentMethodTypes.UPI_COLLECT
                }

                null -> null
            }
        }

        UPISelectedMode.QR -> {
            PaymentMethodTypes.UPI_QR
        }

        UPISelectedMode.VPA -> {
            PaymentMethodTypes.UPI_COLLECT
        }
    }

    private fun getIntentItemAppIdForComponentStateOrNull(outputData: UPIOutputData) =
        if (outputData.selectedMode == UPISelectedMode.INTENT) {
            (outputData.selectedUPIIntentItem as? UPIIntentItem.PaymentApp)?.id
        } else {
            null
        }

    private fun getVirtualPaymentAddress(outputData: UPIOutputData) = when (outputData.selectedMode) {
        UPISelectedMode.INTENT -> {
            when (outputData.selectedUPIIntentItem) {
                is UPIIntentItem.ManualInput -> {
                    outputData.intentVirtualPaymentAddressFieldState.value
                }

                else -> null
            }
        }

        UPISelectedMode.VPA -> {
            outputData.virtualPaymentAddressFieldState.value
        }

        else -> null
    }

    private fun componentStateChanged(componentState: UPIComponentState) {
        _componentStateFlow.tryEmit(componentState)
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.VERBOSE) { "updating outputData to include validation" }
        val outputData = createOutputData(includeValidationErrors = true)
        outputDataChanged(outputData)
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        submitHandler.onSubmit(_componentStateFlow.value)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
