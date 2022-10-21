/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.InputDetail
import com.adyen.checkout.components.model.paymentmethods.Issuer
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultPayByBankDelegate(
    val paymentMethod: PaymentMethod,
    override val configuration: Configuration
) : PayByBankDelegate {

    private val inputData = PayByBankInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayByBankOutputData> = _outputDataFlow

    override val outputData: PayByBankOutputData = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<PayByBankPaymentMethod>> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(PayByBankComponentViewType)

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun getViewProvider(): ViewProvider = PayByBankViewProvider

    override fun updateInputData(update: PayByBankInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = PayByBankOutputData(inputData.selectedIssuer)

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

    override fun getIssuers(): List<IssuerModel> {
        return paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()
    }

    private fun List<Issuer>.mapToModel(): List<IssuerModel> =
        this.mapNotNull { issuer ->
            val (id, name, isDisabled) = issuer
            if (!isDisabled && id != null && name != null) {
                IssuerModel(id, name)
            } else {
                null
            }
        }

    private fun List<InputDetail>?.getLegacyIssuers(): List<IssuerModel> =
        this.orEmpty()
            .flatMap { it.items.orEmpty() }
            .mapNotNull { item ->
                val (id, name) = item
                if (id != null && name != null) {
                    IssuerModel(id, name)
                } else {
                    null
                }
            }
}
