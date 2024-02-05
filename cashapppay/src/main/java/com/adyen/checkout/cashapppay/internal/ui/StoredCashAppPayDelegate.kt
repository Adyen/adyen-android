/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParams
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayInputData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class StoredCashAppPayDelegate(
    private val analyticsRepository: AnalyticsRepository,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: CashAppPayComponentParams,
) : CashAppPayDelegate {

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<CashAppPayComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val submitChannel = bufferedChannel<CashAppPayComponentState>()
    override val submitFlow: Flow<CashAppPayComponentState> = submitChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        setupAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "setupAnalytics" }
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    private fun onState(componentState: CashAppPayComponentState) {
        if (componentState.isValid) {
            submitChannel.trySend(componentState)
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<CashAppPayComponentState>) -> Unit
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

    override fun updateInputData(update: CashAppPayInputData.() -> Unit) {
        adyenLog(AdyenLogLevel.WARN) { "updateInputData should not be called for stored Cash App Pay" }
    }

    private fun createComponentState(): CashAppPayComponentState {
        val cashAppPayPaymentMethod = CashAppPayPaymentMethod(
            type = paymentMethod.type,
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId(),
            storedPaymentMethodId = paymentMethod.id,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = cashAppPayPaymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return CashAppPayComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
    }
}
