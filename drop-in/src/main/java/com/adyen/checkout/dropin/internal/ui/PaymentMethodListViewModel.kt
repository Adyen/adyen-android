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

internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : ViewModel() {

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
