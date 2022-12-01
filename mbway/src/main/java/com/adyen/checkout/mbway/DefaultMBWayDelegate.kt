/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.mbway

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.PaymentComponentUiEvent
import com.adyen.checkout.components.ui.PaymentComponentUiState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.CountryInfo
import com.adyen.checkout.components.util.CountryUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultMBWayDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    override val componentParams: GenericComponentParams,
    private val analyticsRepository: AnalyticsRepository,
    private val submitHandler: SubmitHandler,
) : MBWayDelegate {

    private val inputData = MBWayInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<MBWayOutputData> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<MBWayPaymentMethod>> = _componentStateFlow

    override val outputData: MBWayOutputData get() = _outputDataFlow.value

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(MbWayComponentViewType)

    private val submitChannel: Channel<PaymentComponentState<MBWayPaymentMethod>> = bufferedChannel()
    override val submitFlow: Flow<PaymentComponentState<MBWayPaymentMethod>> = submitChannel.receiveAsFlow()

    private val _uiStateFlow = MutableStateFlow<PaymentComponentUiState>(PaymentComponentUiState.Idle)
    override val uiStateFlow: Flow<PaymentComponentUiState> = _uiStateFlow

    private val uiEventChannel: Channel<PaymentComponentUiEvent> = bufferedChannel()
    override val uiEventFlow: Flow<PaymentComponentUiEvent> = uiEventChannel.receiveAsFlow()

    init {
        updateComponentState(outputData)
    }

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
        callback: (PaymentComponentEvent<PaymentComponentState<MBWayPaymentMethod>>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: MBWayInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        val outputData = createOutputData()
        outputDataChanged(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData(): MBWayOutputData {
        val sanitizedNumber = inputData.localPhoneNumber.trimStart('0')
        return MBWayOutputData(inputData.countryCode + sanitizedNumber)
    }

    private fun outputDataChanged(outputData: MBWayOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: MBWayOutputData) {
        val componentState = createComponentState(outputData)
        componentStateChanged(componentState)
    }

    private fun createComponentState(
        outputData: MBWayOutputData = this.outputData
    ): PaymentComponentState<MBWayPaymentMethod> {
        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            telephoneNumber = outputData.mobilePhoneNumberFieldState.value
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod
        )

        return PaymentComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true
        )
    }

    override fun getSupportedCountries(): List<CountryInfo> = CountryUtils.getCountries(SUPPORTED_COUNTRIES)

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(
            state = state,
            submitChannel = submitChannel,
            uiEventChannel = uiEventChannel,
            uiStateChannel = _uiStateFlow
        )
    }

    override fun onCleared() {
        removeObserver()
    }

    private fun componentStateChanged(componentState: PaymentComponentState<MBWayPaymentMethod>) {
        _componentStateFlow.tryEmit(componentState)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
