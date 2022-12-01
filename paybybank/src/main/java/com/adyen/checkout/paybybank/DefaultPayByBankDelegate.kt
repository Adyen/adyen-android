/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.GenericComponentParams
import com.adyen.checkout.components.model.paymentmethods.InputDetail
import com.adyen.checkout.components.model.paymentmethods.Issuer
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.IssuerModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class DefaultPayByBankDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    override val componentParams: GenericComponentParams,
    private val analyticsRepository: AnalyticsRepository,
) : PayByBankDelegate {

    private val inputData = PayByBankInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayByBankOutputData> = _outputDataFlow

    override val outputData: PayByBankOutputData = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<PayByBankPaymentMethod>> = _componentStateFlow

    private val _viewFlow = MutableStateFlow<ComponentViewType?>(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    init {
        val hasIssuers = paymentMethod.issuers?.isNotEmpty() == true
        if (!hasIssuers) {
            _componentStateFlow.tryEmit(createValidComponentState())
        } else {
            _viewFlow.tryEmit(PayByBankComponentViewType)
        }
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
        callback: (PaymentComponentEvent<PaymentComponentState<PayByBankPaymentMethod>>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun updateInputData(update: PayByBankInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = PayByBankOutputData(
        selectedIssuer = inputData.selectedIssuer,
        issuers = filterByQuery()
    )

    private fun filterByQuery(): List<IssuerModel> = inputData.query?.let { query ->
        getIssuers().filter { issuerModel ->
            issuerModel.name.contains(query, ignoreCase = true)
        }
    } ?: getIssuers()

    private fun updateComponentState(outputData: PayByBankOutputData) {
        _componentStateFlow.tryEmit(createComponentState(outputData))
    }

    private fun createComponentState(outputData: PayByBankOutputData = this.outputData): PaymentComponentState<PayByBankPaymentMethod> {
        val payByBankPaymentMethod = PayByBankPaymentMethod(
            type = getPaymentMethodType(), issuer = outputData.selectedIssuer?.id ?: ""
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = payByBankPaymentMethod
        )

        return PaymentComponentState(
            data = paymentComponentData, isInputValid = outputData.isValid, isReady = true
        )
    }

    private fun createValidComponentState(): PaymentComponentState<PayByBankPaymentMethod> {
        val payByBankPaymentMethod = PayByBankPaymentMethod(
            type = getPaymentMethodType()
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = payByBankPaymentMethod
        )
        return PaymentComponentState(
            data = paymentComponentData, isInputValid = true, isReady = true
        )
    }

    override fun getIssuers(): List<IssuerModel> {
        return paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()
    }

    private fun List<Issuer>.mapToModel(): List<IssuerModel> =
        this.mapNotNull { (id, name, isDisabled) ->
            if (!isDisabled && id != null && name != null) {
                IssuerModel(id, name)
            } else {
                null
            }
        }

    private fun List<InputDetail>?.getLegacyIssuers(): List<IssuerModel> =
        this.orEmpty()
            .flatMap { it.items.orEmpty() }
            .mapNotNull { (id, name) ->
                if (id != null && name != null) {
                    IssuerModel(id, name)
                } else {
                    null
                }
            }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
