/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */
package com.adyen.checkout.onlinebankingcore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ViewProvidingComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class OnlineBankingComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    savedStateHandle: SavedStateHandle,
    final override val delegate: OnlineBankingDelegate<IssuerListPaymentMethodT>,
    configuration: OnlineBankingConfiguration
) : BasePaymentComponent<
    OnlineBankingConfiguration,
    OnlineBankingInputData,
    OnlineBankingOutputData,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(savedStateHandle, delegate, configuration),
    ViewProvidingComponent {

    override val inputData: OnlineBankingInputData =
        OnlineBankingInputData()

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

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
}
