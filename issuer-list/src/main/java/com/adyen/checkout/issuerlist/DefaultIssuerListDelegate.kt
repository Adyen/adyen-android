/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultIssuerListDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    override val configuration: IssuerListConfiguration,
    private val paymentMethod: PaymentMethod,
    private val typedPaymentMethodFactory: () -> IssuerListPaymentMethodT,
) : IssuerListDelegate<IssuerListPaymentMethodT> {

    override val inputData: IssuerListInputData = IssuerListInputData()

    private val _outputDataFlow = MutableStateFlow<IssuerListOutputData?>(null)
    override val outputDataFlow: Flow<IssuerListOutputData?> = _outputDataFlow

    override val outputData: IssuerListOutputData?
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<IssuerListPaymentMethodT>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>?> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(getIssuerListComponentViewType())

    private fun getIssuerListComponentViewType(): IssuerListComponentViewType {
        return when (configuration.viewType) {
            IssuerListViewType.RECYCLER_VIEW -> IssuerListComponentViewType.RECYCLER_VIEW
            IssuerListViewType.SPINNER_VIEW -> IssuerListComponentViewType.SPINNER_VIEW
        }
    }

    override fun getIssuers(): List<IssuerModel> =
        paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()

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

    override fun getViewProvider(): ViewProvider = IssuerListViewProvider
}
