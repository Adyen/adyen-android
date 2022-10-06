/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly.
 */
abstract class IssuerListComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    savedStateHandle: SavedStateHandle,
    private val issuerListDelegate: IssuerListDelegate<IssuerListPaymentMethodT>,
    configuration: IssuerListConfiguration
) : BasePaymentComponent<
    IssuerListConfiguration,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(
    savedStateHandle,
    issuerListDelegate,
    configuration
) {
    val issuers: List<IssuerModel>
        get() = issuerListDelegate.getIssuers()

    val paymentMethodType: String
        get() = issuerListDelegate.getPaymentMethodType()

    init {
        issuerListDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }
}
