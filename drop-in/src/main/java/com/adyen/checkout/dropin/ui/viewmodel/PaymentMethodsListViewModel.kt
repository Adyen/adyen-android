/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/11/2020.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentMethodsListViewModel constructor(private val delegate: PaymentMethodsListDelegate) : ViewModel() {

    private val paymentMethodsMutableFlow = MutableStateFlow<List<PaymentMethodListItem>>(emptyList())
    internal val paymentMethodsFlow: StateFlow<List<PaymentMethodListItem>> = paymentMethodsMutableFlow

    init {
        delegate.paymentMethodsFlow.onEach {
            paymentMethodsMutableFlow.tryEmit(it)
        }.launchIn(viewModelScope)
    }

    internal fun getPaymentMethod(index: Int): PaymentMethod {
        return delegate.getPaymentMethod(index)
    }

    internal fun removePaymentMethodWithId(id: String) {
        delegate.removePaymentMethodWithId(id)
    }
}
