/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import kotlin.reflect.KClass

internal class PreselectedPaymentMethodViewModel(
    private val storedPaymentMethod: StoredPaymentMethod,
) : ViewModel() {

    class Factory(
        private val storedPaymentMethod: StoredPaymentMethod,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return PreselectedPaymentMethodViewModel(
                storedPaymentMethod = storedPaymentMethod,
            ) as T
        }
    }
}
