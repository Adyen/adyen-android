/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/11/2024.
 */

package com.adyen.checkout.paybybankus.internal

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSOutputData
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal class StoredPayByBankUSDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<PayByBankUSComponentState>,
) : PayByBankUSDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayByBankUSOutputData> = _outputDataFlow

    override val outputData: PayByBankUSOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PayByBankUSComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<PayByBankUSComponentState> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PayByBankUSComponentState>) -> Unit
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

    private fun createOutputData() = PayByBankUSOutputData(
        // No brand list to be displayed
        brandList = emptyList(),
    )

    private fun createComponentState(): PayByBankUSComponentState {
        val payByBankUsPaymentMethod = PayByBankUSPaymentMethod(
            type = getPaymentMethodType(),
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            storedPaymentMethodId = storedPaymentMethod.id,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = payByBankUsPaymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return PayByBankUSComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(storedPaymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

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
        analyticsManager.clear(this)
    }
}
