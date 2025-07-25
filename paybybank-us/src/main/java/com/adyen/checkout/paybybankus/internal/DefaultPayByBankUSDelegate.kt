/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.R
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSBrandLogo
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.old.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.LogoItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.TextItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal class DefaultPayByBankUSDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<PayByBankUSComponentState>,
) : PayByBankUSDelegate, ButtonDelegate, UIStateDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayByBankUSOutputData> = _outputDataFlow

    override val outputData: PayByBankUSOutputData = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PayByBankUSComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(PayByBankUSComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<PayByBankUSComponentState> = submitHandler.submitFlow
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

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    private fun createOutputData() = PayByBankUSOutputData(
        brandList = getBrandList(),
    )

    private fun createComponentState(): PayByBankUSComponentState {
        val payByBankUsPaymentMethod = PayByBankUSPaymentMethod(
            type = getPaymentMethodType(),
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
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
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

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

    private fun getBrandList(): List<LogoTextItem> {
        return listOf(
            PayByBankUSBrandLogo.entries.map {
                LogoItem(
                    it.path,
                    componentParams.environment,
                )
            },
            listOf(TextItem(R.string.checkout_pay_by_bank_us_more)),
        ).flatten()
    }
}
