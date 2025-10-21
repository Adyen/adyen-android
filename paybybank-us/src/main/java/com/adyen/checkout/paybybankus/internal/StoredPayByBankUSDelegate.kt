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
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions")
internal class StoredPayByBankUSDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
) : PayByBankUSDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayByBankUSOutputData> = _outputDataFlow

    override val outputData: PayByBankUSOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PayByBankUSComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val submitChannel = bufferedChannel<PayByBankUSComponentState>()
    override val submitFlow: Flow<PayByBankUSComponentState> = submitChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        initializeAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = storedPaymentMethod.type.orEmpty(),
            isStoredPaymentMethod = true,
        )
        analyticsManager.trackEvent(event)
    }

    private fun onState(payByBankUSComponentState: PayByBankUSComponentState) {
        if (payByBankUSComponentState.isValid) {
            val event = GenericEvents.submit(storedPaymentMethod.type.orEmpty())
            analyticsManager.trackEvent(event)

            submitChannel.trySend(payByBankUSComponentState)
        }
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
            sdkData = sdkDataProvider.createEncodedSdkData(),
        )

        return PayByBankUSComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        // no ops
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
