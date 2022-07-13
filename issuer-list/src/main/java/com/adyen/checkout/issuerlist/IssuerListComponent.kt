/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.paymentmethods.InputDetail
import com.adyen.checkout.components.model.paymentmethods.Issuer
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData

abstract class IssuerListComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    savedStateHandle: SavedStateHandle,
    genericPaymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: IssuerListConfiguration
) : BasePaymentComponent<
    IssuerListConfiguration,
    IssuerListInputData,
    IssuerListOutputData,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(
    savedStateHandle,
    genericPaymentMethodDelegate,
    configuration
) {
    val issuersLiveData = MutableLiveData<List<IssuerModel>>()

    override var inputData: IssuerListInputData = IssuerListInputData()

    private fun initComponent(paymentMethod: PaymentMethod) {
        val issuersList = paymentMethod.issuers
        if (issuersList != null) {
            initIssuers(issuersList)
        } else {
            initLegacyIssuers(paymentMethod.details)
        }
    }

    private fun initIssuers(issuerList: List<Issuer>) {
        val issuerModelList: MutableList<IssuerModel> = ArrayList()
        for ((id, name, isDisabled) in issuerList) {
            if (!isDisabled) {
                val issuerModel = IssuerModel(id!!, name!!)
                issuerModelList.add(issuerModel)
            }
        }
        issuersLiveData.value = issuerModelList
    }

    private fun initLegacyIssuers(details: List<InputDetail>?) {
        for ((items) in details ?: emptyList()) {
            val issuers: MutableList<IssuerModel> = ArrayList()
            for ((id, name) in items ?: emptyList()) {
                issuers.add(IssuerModel(id.orEmpty(), name.orEmpty()))
            }
            issuersLiveData.value = issuers
        }
    }

    override fun onInputDataChanged(inputData: IssuerListInputData) {
        // can also reuse instance if we implement equals properly
        notifyOutputDataChanged(IssuerListOutputData(inputData.selectedIssuer))
        createComponentState()
    }

    private fun createComponentState() {
        val issuerListPaymentMethod = instantiateTypedPaymentMethod()
        val selectedIssuer = outputData?.selectedIssuer
        issuerListPaymentMethod.type = paymentMethodDelegate.getPaymentMethodType()
        issuerListPaymentMethod.issuer = selectedIssuer?.id ?: ""
        val isInputValid: Boolean = outputData?.isValid == true
        val paymentComponentData = PaymentComponentData<IssuerListPaymentMethodT>()
        paymentComponentData.paymentMethod = issuerListPaymentMethod
        notifyStateChanged(PaymentComponentState(paymentComponentData, isInputValid, true))
    }

    protected abstract fun instantiateTypedPaymentMethod(): IssuerListPaymentMethodT
    val paymentMethodType: String
        get() = paymentMethodDelegate.getPaymentMethodType()

    init {
        initComponent(genericPaymentMethodDelegate.paymentMethod)
    }
}
