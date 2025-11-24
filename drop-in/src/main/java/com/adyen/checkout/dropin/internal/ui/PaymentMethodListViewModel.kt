/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.data.model.format
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : ViewModel() {

    private val _viewState = MutableStateFlow(createInitialViewState())
    val viewState: StateFlow<PaymentMethodListViewState> = _viewState.asStateFlow()

    private fun createInitialViewState(): PaymentMethodListViewState {
        val paymentOptionsSection = paymentMethodsApiResponse.paymentMethods?.let { paymentMethods ->
            PaymentOptionsSection(
                title = "Other options",
                options = paymentMethods,
            )
        }

        return PaymentMethodListViewState(
            amount = dropInParams.amount.format(dropInParams.shopperLocale),
            paymentOptionsSection = paymentOptionsSection,
        )
    }

    class Factory(
        private val dropInParams: DropInParams,
        private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodListViewModel(
                dropInParams = dropInParams,
                paymentMethodsApiResponse = paymentMethodsApiResponse,
            ) as T
        }
    }
}
