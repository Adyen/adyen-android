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
import com.adyen.checkout.components.core.App
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
import com.adyen.checkout.upi.internal.ui.model.UPICollectItem
import com.adyen.checkout.upi.internal.ui.model.UPIInputData
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

    // This allows moving validation to the delegate without changing the communication structure between delegate and
    // the view. After we refactor state handling and validation logic, this can be improved.
    private var cachedCollectVirtualPaymentAddress: String = ""

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

    private fun createOutputData() = with(inputData) {
        // TODO: Mock data to be removed
        paymentMethod.apps = listOf(
            App("bhim", "BHIM"),
            App("gpay", "Google Pay"),
            App("PhonePe", "phonepe"),
        )

        val appIds = paymentMethod.apps
        val collectVirtualPaymentAddressFieldState = validateVirtualPaymentAddress(collectVirtualPaymentAddress)
        var defaultSelectedCollectItem: UPICollectItem? = null
        val availableModes = if (!appIds.isNullOrEmpty()) {
            val collectItemList = createCollectItems(
                appIds,
                componentParams.environment,
                collectVirtualPaymentAddressFieldState,
            )
            // TODO: This could also be passed to the UPIAppsAdapter to show an item as default.
            defaultSelectedCollectItem = collectItemList.firstOrNull()
            listOf(UPIMode.Collect(collectItemList), UPIMode.Qr)
        } else {
            listOf(UPIMode.Vpa, UPIMode.Qr)
        }

        UPIOutputData(
            selectedMode = selectedMode ?: availableModes.first().mapToSelectedMode(),
            selectedUPICollectItem = selectedUPICollectItem ?: defaultSelectedCollectItem,
            availableModes = availableModes,
            virtualPaymentAddressFieldState = validateVirtualPaymentAddress(vpaVirtualPaymentAddress),
            collectVirtualPaymentAddressFieldState = collectVirtualPaymentAddressFieldState,
        )
    }

    private fun createCollectItems(
        apps: List<App>,
        environment: Environment,
        collectVirtualPaymentAddressFieldState: FieldState<String>,
    ): List<UPICollectItem> {
        val paymentApps = apps.mapToPaymentApp(environment = environment)
        val manualInputErrorMessageId =
            getValidationErrorResourceIdOrNull(collectVirtualPaymentAddressFieldState.validation)

        return mutableListOf<UPICollectItem>().apply {
            addAll(paymentApps)
            add(UPICollectItem.GenericApp)
            add(UPICollectItem.ManualInput(manualInputErrorMessageId))
        }
    }

    private fun getValidationErrorResourceIdOrNull(validation: Validation?): Int? =
        (validation as? Validation.Invalid)?.reason

    private fun validateVirtualPaymentAddress(virtualPaymentAddress: String?): FieldState<String> =
        if (virtualPaymentAddress == null) {
            FieldState("", Validation.Valid)
        } else if (virtualPaymentAddress.isNotBlank()) {
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
            appId = getCollectItemAppIdForComponentStateOrNull(outputData),
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
        UPISelectedMode.COLLECT -> {
            when (outputData.selectedUPICollectItem) {
                is UPICollectItem.PaymentApp -> {
                    PaymentMethodTypes.UPI_INTENT
                }

                UPICollectItem.GenericApp -> {
                    PaymentMethodTypes.UPI_INTENT
                }

                is UPICollectItem.ManualInput -> {
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

    private fun getCollectItemAppIdForComponentStateOrNull(outputData: UPIOutputData) =
        if (outputData.selectedMode == UPISelectedMode.COLLECT) {
            (outputData.selectedUPICollectItem as? UPICollectItem.PaymentApp)?.id
        } else {
            null
        }

    private fun getVirtualPaymentAddress(outputData: UPIOutputData) = when (outputData.selectedMode) {
        UPISelectedMode.COLLECT -> {
            when (outputData.selectedUPICollectItem) {
                is UPICollectItem.ManualInput -> {
                    outputData.collectVirtualPaymentAddressFieldState.value
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

    override fun updateCollectVirtualPaymentAddress(value: String) {
        cachedCollectVirtualPaymentAddress = value

        // This makes sure that the field validation gets updated for the delegate too and not only for the input field
        if (inputData.collectVirtualPaymentAddress != null) {
            updateInputData {
                collectVirtualPaymentAddress = null
            }
        }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onSubmit() {
        // This allows moving validation to the delegate without changing the communication structure between delegate
        // and the view. After we refactor state handling and validation logic, this can be improved.
        updateInputData {
            collectVirtualPaymentAddress = cachedCollectVirtualPaymentAddress
        }

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
