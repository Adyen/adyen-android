package com.adyen.checkout.sepa

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.repository.ObserverRepository
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultSepaDelegate(
    private val observerRepository: ObserverRepository,
    override val configuration: SepaConfiguration,
    private val paymentMethod: PaymentMethod
) : SepaDelegate {

    private val inputData: SepaInputData = SepaInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<SepaOutputData> = _outputDataFlow

    override val outputData: SepaOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(SepaComponentViewType)

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PaymentComponentState<SepaPaymentMethod>>) -> Unit
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

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: SepaInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")

        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = SepaOutputData(inputData.name, inputData.iban)

    @VisibleForTesting
    internal fun updateComponentState(outputData: SepaOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: SepaOutputData = this.outputData
    ): PaymentComponentState<SepaPaymentMethod> {
        val paymentMethod = SepaPaymentMethod(
            type = SepaPaymentMethod.PAYMENT_METHOD_TYPE,
            ownerName = outputData.ownerNameField.value,
            iban = outputData.ibanNumberField.value
        )
        val paymentComponentData = PaymentComponentData(paymentMethod)
        return PaymentComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    override fun onCleared() {
        removeObserver()
    }

    override fun getViewProvider(): ViewProvider = SepaViewProvider

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
