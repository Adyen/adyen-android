/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultIssuerListDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    private val paymentMethod: PaymentMethod,
    private val typedPaymentMethodFactory: () -> IssuerListPaymentMethodT,
) : IssuerListDelegate<IssuerListPaymentMethodT> {

    private val _outputDataFlow = MutableStateFlow<IssuerListOutputData?>(null)
    override val outputDataFlow: Flow<IssuerListOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<IssuerListPaymentMethodT>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>?> = _componentStateFlow

    override fun getIssuers(): List<IssuerModel> {
        return paymentMethod.issuers?.let {
            IssuersUtils.getIssuers(it)
        } ?: IssuersUtils.getLegacyIssuers(paymentMethod.details)
    }

    override fun onInputDataChanged(inputData: IssuerListInputData) {
        val outputData = IssuerListOutputData(inputData.selectedIssuer)

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    override fun createComponentState(outputData: IssuerListOutputData) {
        val issuerListPaymentMethod = typedPaymentMethodFactory()
        issuerListPaymentMethod.type = getPaymentMethodType()
        issuerListPaymentMethod.issuer = outputData.selectedIssuer?.id ?: ""

        val paymentComponentData = PaymentComponentData<IssuerListPaymentMethodT>()
        paymentComponentData.paymentMethod = issuerListPaymentMethod

        val state = PaymentComponentState(paymentComponentData, outputData.isValid, true)
        _componentStateFlow.tryEmit(state)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }
}
