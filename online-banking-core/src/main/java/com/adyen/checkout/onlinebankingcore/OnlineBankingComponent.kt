/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */
package com.adyen.checkout.onlinebankingcore

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class OnlineBankingComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    savedStateHandle: SavedStateHandle,
    private val delegate: OnlineBankingDelegate<IssuerListPaymentMethodT>,
    configuration: OnlineBankingConfiguration
) : BasePaymentComponent<
    OnlineBankingConfiguration,
    OnlineBankingInputData,
    OnlineBankingOutputData,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(savedStateHandle, delegate, configuration) {

    abstract val termsAndConditionsUrl: String

    override val inputData: OnlineBankingInputData =
        OnlineBankingInputData()

    val issuers: List<OnlineBankingModel>
        get() = delegate.getIssuers()

    init {
        delegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        delegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .filterNotNull()
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun onInputDataChanged(inputData: OnlineBankingInputData) {
        delegate.onInputDataChanged(inputData)
    }

    internal fun openTermsAndConditionsPdf(context: Context) {
        delegate.openPdf(context, termsAndConditionsUrl)
    }
}
