/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.ObserverRepository
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultIssuerListDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    private val observerRepository: ObserverRepository,
    override val configuration: IssuerListConfiguration,
    private val paymentMethod: PaymentMethod,
    private val typedPaymentMethodFactory: () -> IssuerListPaymentMethodT,
) : IssuerListDelegate<IssuerListPaymentMethodT> {

    private val inputData: IssuerListInputData = IssuerListInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<IssuerListOutputData> = _outputDataFlow

    override val outputData: IssuerListOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(getIssuerListComponentViewType())

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PaymentComponentState<IssuerListPaymentMethodT>>) -> Unit
    ) {
        observerRepository.observePaymentComponentEvents(
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

    private fun getIssuerListComponentViewType(): IssuerListComponentViewType {
        return when (configuration.viewType) {
            IssuerListViewType.RECYCLER_VIEW -> IssuerListComponentViewType.RECYCLER_VIEW
            IssuerListViewType.SPINNER_VIEW -> IssuerListComponentViewType.SPINNER_VIEW
        }
    }

    override fun getIssuers(): List<IssuerModel> =
        paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()

    override fun updateInputData(update: IssuerListInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = IssuerListOutputData(inputData.selectedIssuer)

    @VisibleForTesting
    internal fun updateComponentState(outputData: IssuerListOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: IssuerListOutputData = this.outputData
    ): PaymentComponentState<IssuerListPaymentMethodT> {
        val issuerListPaymentMethod = typedPaymentMethodFactory()
        issuerListPaymentMethod.type = getPaymentMethodType()
        issuerListPaymentMethod.issuer = outputData.selectedIssuer?.id ?: ""

        val paymentComponentData = PaymentComponentData<IssuerListPaymentMethodT>()
        paymentComponentData.paymentMethod = issuerListPaymentMethod

        return PaymentComponentState(paymentComponentData, outputData.isValid, true)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
    }

    override fun getViewProvider(): ViewProvider = IssuerListViewProvider
}
