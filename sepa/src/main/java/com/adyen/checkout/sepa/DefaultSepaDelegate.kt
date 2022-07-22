package com.adyen.checkout.sepa

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultSepaDelegate(private val paymentMethod: PaymentMethod) : SepaDelegate {

    private val _outputDataFlow = MutableStateFlow<SepaOutputData?>(null)
    override val outputDataFlow: Flow<SepaOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<SepaPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>?> = _componentStateFlow

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onInputDataChanged(inputData: SepaInputData) {
        Logger.v(TAG, "onInputDataChanged")
        val outputData = SepaOutputData(inputData.name, inputData.iban)
        outputDataChanged(outputData)
        createComponentState(outputData)
    }

    private fun outputDataChanged(outputData: SepaOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    override fun createComponentState(outputData: SepaOutputData) {
        val paymentMethod = SepaPaymentMethod(
            type = SepaPaymentMethod.PAYMENT_METHOD_TYPE,
            ownerName = outputData.ownerNameField.value,
            iban = outputData.ibanNumberField.value
        )
        val paymentComponentData = PaymentComponentData(paymentMethod)
        componentStateChanged(
            PaymentComponentState(
                paymentComponentData,
                outputData.isValid,
                true
            )
        )
    }

    private fun componentStateChanged(componentState: PaymentComponentState<SepaPaymentMethod>) {
        _componentStateFlow.tryEmit(componentState)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
