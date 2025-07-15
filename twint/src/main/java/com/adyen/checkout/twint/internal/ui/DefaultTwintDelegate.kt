/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.paymentmethod.TwintPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParams
import com.adyen.checkout.twint.internal.ui.model.TwintInputData
import com.adyen.checkout.twint.internal.ui.model.TwintOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class DefaultTwintDelegate(
    private val submitHandler: SubmitHandler<TwintComponentState>,
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: TwintComponentParams,
) : TwintDelegate, ButtonDelegate {

    private val inputData = TwintInputData()

    private var outputData = createOutputData()

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<TwintComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(TwintComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<TwintComponentState> = getTrackedSubmitFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)

        if (!isConfirmationRequired()) {
            initiatePayment()
        }
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
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
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

    override fun updateInputData(update: TwintInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        outputData = createOutputData()
        updateComponentState(outputData)
    }

    private fun createOutputData(): TwintOutputData {
        return TwintOutputData(
            isStorePaymentSelected = inputData.isStorePaymentSelected,
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: TwintOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: TwintOutputData = this.outputData
    ): TwintComponentState {
        val paymentMethod = TwintPaymentMethod(
            type = paymentMethod.type,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            subtype = getSubtype(),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
            storePaymentMethod = if (componentParams.showStorePaymentField) outputData.isStorePaymentSelected else null,
        )

        return TwintComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    private fun getSubtype(): String? {
        return when (componentParams.actionHandlingMethod) {
            ActionHandlingMethod.PREFER_NATIVE -> SDK_SUBTYPE
            ActionHandlingMethod.PREFER_WEB -> null
        }
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        if (isConfirmationRequired()) {
            initiatePayment()
        }
    }

    private fun initiatePayment() {
        _viewFlow.tryEmit(PaymentInProgressViewType)

        val state = _componentStateFlow.value
        submitHandler.onSubmit(state = state)
    }

    override fun isConfirmationRequired(): Boolean =
        _viewFlow.value is ButtonComponentViewType &&
            componentParams.showStorePaymentField

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    internal fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String =
        paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }

    companion object {
        @VisibleForTesting
        internal const val SDK_SUBTYPE = "sdk"
    }
}
