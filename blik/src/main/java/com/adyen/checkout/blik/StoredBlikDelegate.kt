/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.PaymentComponentUiEvent
import com.adyen.checkout.components.ui.PaymentComponentUiState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class StoredBlikDelegate(
    private val observerRepository: PaymentObserverRepository,
    override val componentParams: GenericComponentParams,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsRepository: AnalyticsRepository,
    private val buttonDelegate: ButtonDelegate,
    private val submitHandler: SubmitHandler
) : BlikDelegate, ButtonDelegate by buttonDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BlikOutputData> = _outputDataFlow

    override val outputData: BlikOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(BlikComponentViewType)

    private val _uiStateFlow = MutableStateFlow<PaymentComponentUiState>(PaymentComponentUiState.Idle)
    override val uiStateFlow: Flow<PaymentComponentUiState> = _uiStateFlow

    private val _uiEventChannel: Channel<PaymentComponentUiEvent> = bufferedChannel()
    override val uiEventFlow: Flow<PaymentComponentUiEvent> = _uiEventChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        sendAnalyticsEvent(coroutineScope)
    }

    private fun sendAnalyticsEvent(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "sendAnalyticsEvent")
        coroutineScope.launch {
            analyticsRepository.sendAnalyticsEvent()
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PaymentComponentState<BlikPaymentMethod>>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = buttonDelegate.submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: BlikInputData.() -> Unit) {
        Logger.e(TAG, "updateInputData should not be called in StoredBlikDelegate")
    }

    private fun createOutputData() = BlikOutputData(blikCode = "")

    private fun createComponentState(): PaymentComponentState<BlikPaymentMethod> {
        val paymentMethod = BlikPaymentMethod(
            type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
            storedPaymentMethodId = storedPaymentMethod.id
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod
        )

        return PaymentComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true
        )
    }

    override fun requiresInput(): Boolean = false

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
